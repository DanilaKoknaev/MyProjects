package org.example.finance.counterparty.repository;

import org.example.finance.counterparty.model.Counterparty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CounterpartyRepository extends JpaRepository<Counterparty, UUID> {
}
