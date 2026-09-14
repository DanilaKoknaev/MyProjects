package org.example.finance.transaction.service;

import org.example.finance.account.model.Account;
import org.example.finance.account.repository.AccountRepository;
import org.example.finance.audit.model.AuditAction;
import org.example.finance.audit.service.AuditService;
import org.example.finance.category.model.CategoryType;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.common.exception.NotFoundException;
import org.example.finance.counterparty.repository.CounterpartyRepository;
import org.example.finance.security.CurrentUserService;
import org.example.finance.transaction.dto.TransactionDto;
import org.example.finance.transaction.dto.TransactionRequest;
import org.example.finance.transaction.model.FinancialTransaction;
import org.example.finance.transaction.model.TransactionEntry;
import org.example.finance.transaction.model.TransactionStatus;
import org.example.finance.transaction.model.TransactionType;
import org.example.finance.transaction.repository.TransactionRepository;
import org.example.finance.user.model.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@Transactional
public class TransactionService {
    private final TransactionRepository repository;
    private final AccountRepository accounts;
    private final CategoryRepository categories;
    private final CounterpartyRepository counterparties;
    private final CurrentUserService current;
    private final AuditService audit;

    public TransactionService(TransactionRepository r, AccountRepository a, CategoryRepository c, CounterpartyRepository cp, CurrentUserService current, AuditService audit) {
        repository = r;
        accounts = a;
        categories = c;
        counterparties = cp;
        this.current = current;
        this.audit = audit;
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public TransactionDto create(TransactionRequest r) {
        var actor = current.get();
        var source = account(r.sourceAccountId());
        current.requireDepartment(actor, source.getDepartment().getId());
        validate(r, source);
        var tx = new FinancialTransaction(r.type(), r.amount(), r.currency(), r.operationDate(), source.getDepartment(), category(r), counterparty(r.counterpartyId()), actor);
        tx.replaceEntries(entries(tx, r, source));
        repository.save(tx);
        audit.record(actor.getId(), tx.getId(), AuditAction.CREATED, "SUCCESS", "Draft transaction created");
        return dto(tx);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public TransactionDto update(UUID id, TransactionRequest r) {
        var actor = current.get();
        var tx = get(id);
        current.requireDepartment(actor, tx.getDepartment().getId());
        if (r.type() != tx.getType())
            throw new IllegalStateException("Transaction type cannot be changed");
        if (!Objects.equals(r.currency(), tx.getCurrency()))
            throw new IllegalStateException("Transaction currency cannot be changed");
        var source = account(r.sourceAccountId());
        if (!Objects.equals(source.getDepartment().getId(), tx.getDepartment().getId()))
            throw new IllegalArgumentException("Source account department cannot be changed");
        validate(r, source);
        tx.changeDetails(r.amount(), r.operationDate(), category(r), counterparty(r.counterpartyId()));
        tx.replaceEntries(entries(tx, r, source));
        audit.record(actor.getId(), id, AuditAction.UPDATED, "SUCCESS", "Draft transaction updated");
        return dto(tx);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public TransactionDto confirm(UUID id) {
        var actor = current.get();
        var tx = get(id);
        current.requireDepartment(actor, tx.getDepartment().getId());
        tx.confirm();
        audit.record(actor.getId(), id, AuditAction.CONFIRMED, "SUCCESS", "Transaction confirmed");
        return dto(tx);
    }

    @PreAuthorize("hasAnyRole('ADMIN','ACCOUNTANT')")
    public TransactionDto reverse(UUID id) {
        var actor = current.get();
        var original = get(id);
        current.requireDepartment(actor, original.getDepartment().getId());
        if (original.getReversalOf() != null) throw new IllegalStateException("A reversal cannot be reversed");
        var reversal = new FinancialTransaction(TransactionType.REVERSAL, original.getAmount(), original.getCurrency(), java.time.LocalDate.now(), original.getDepartment(), original.getCategory(), original.getCounterparty(), actor);
        reversal.linkReversal(original);
        for (var e : original.getEntries())
            reversal.addEntry(new TransactionEntry(reversal, e.getAccount(), e.getAmount().negate()));
        reversal.confirm();
        original.markReversed();
        repository.save(reversal);
        audit.record(actor.getId(), id, AuditAction.REVERSED, "SUCCESS", "Reversal " + reversal.getId() + " created");
        return dto(reversal);
    }

    @PreAuthorize("isAuthenticated()")
    public void delete(UUID id) {
        var actor = current.get();
        var tx = get(id);
        try {
            if (actor.getRole() != Role.ADMIN && actor.getRole() != Role.ACCOUNTANT)
                throw new AccessDeniedException("Role cannot delete transactions");
            current.requireDepartment(actor, tx.getDepartment().getId());
            if (tx.getStatus() != TransactionStatus.DRAFT)
                throw new IllegalStateException("Confirmed transaction cannot be deleted; create a reversal");
            audit.recordIndependent(actor.getId(), id, AuditAction.DELETE_ATTEMPT, "SUCCESS", "Draft deleted");
            repository.delete(tx);
        } catch (RuntimeException e) {
            audit.recordIndependent(actor.getId(), id, AuditAction.DELETE_ATTEMPT, "REJECTED", e.getMessage());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Page<TransactionDto> list(Pageable p) {
        var actor = current.get();
        return (current.seesAll(actor) ? repository.findAll(p) : repository.findByDepartmentId(actor.getDepartment().getId(), p)).map(this::dto);
    }

    @Transactional(readOnly = true)
    public TransactionDto find(UUID id) {
        var actor = current.get();
        var tx = get(id);
        current.requireDepartment(actor, tx.getDepartment().getId());
        return dto(tx);
    }

    private FinancialTransaction get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Transaction"));
    }

    private Account account(UUID id) {
        return accounts.findById(id).orElseThrow(() -> new NotFoundException("Account"));
    }

    private org.example.finance.category.model.Category category(TransactionRequest r) {
        if (r.type() == TransactionType.TRANSFER) {
            if (r.categoryId() != null) throw new IllegalArgumentException("Transfer must not have a category");
            return null;
        }
        if (r.categoryId() == null) throw new IllegalArgumentException("Income and expense require a category");
        var c = categories.findById(r.categoryId()).orElseThrow(() -> new NotFoundException("Category"));
        var expected = r.type() == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        if (c.getType() != expected)
            throw new IllegalArgumentException("Category type does not match transaction type");
        return c;
    }

    private org.example.finance.counterparty.model.Counterparty counterparty(UUID id) {
        return id == null ? null : counterparties.findById(id).orElseThrow(() -> new NotFoundException("Counterparty"));
    }

    private void validate(TransactionRequest r, Account source) {
        if (r.type() == TransactionType.REVERSAL)
            throw new IllegalArgumentException("Reversal is created through the reverse command");
        if (!source.isActive()) throw new IllegalStateException("Account is inactive");
        if (!source.getCurrency().equals(r.currency())) throw new IllegalArgumentException("Account currency differs");
        if (r.type() == TransactionType.TRANSFER) {
            if (r.destinationAccountId() == null || r.destinationAccountId().equals(r.sourceAccountId()))
                throw new IllegalArgumentException("Transfer requires another destination account");
            var destination = account(r.destinationAccountId());
            if (!destination.isActive() || !destination.getCurrency().equals(r.currency()))
                throw new IllegalArgumentException("Destination account is inactive or has another currency");
            if (!Objects.equals(destination.getDepartment().getId(), source.getDepartment().getId()))
                throw new IllegalArgumentException("Transfer accounts must belong to the same department");
        } else if (r.destinationAccountId() != null)
            throw new IllegalArgumentException("Destination is allowed only for transfers");
    }

    private List<TransactionEntry> entries(FinancialTransaction tx, TransactionRequest r, Account source) {
        if (r.type() == TransactionType.INCOME) return List.of(new TransactionEntry(tx, source, r.amount()));
        if (r.type() == TransactionType.EXPENSE) return List.of(new TransactionEntry(tx, source, r.amount().negate()));
        return List.of(new TransactionEntry(tx, source, r.amount().negate()), new TransactionEntry(tx, account(r.destinationAccountId()), r.amount()));
    }

    private TransactionDto dto(FinancialTransaction t) {
        return new TransactionDto(t.getId(), t.getType(), t.getStatus(), t.getAmount(), t.getCurrency(), t.getOperationDate(), t.getDepartment().getId(), t.getCategory() == null ? null : t.getCategory().getId(), t.getCounterparty() == null ? null : t.getCounterparty().getId(), t.getAuthor().getId(), t.getReversalOf() == null ? null : t.getReversalOf().getId(), t.getEntries().stream().map(e -> new TransactionDto.EntryDto(e.getAccount().getId(), e.getAmount())).toList());
    }
}
