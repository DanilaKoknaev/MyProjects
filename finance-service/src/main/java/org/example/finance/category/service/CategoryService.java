package org.example.finance.category.service;

import org.example.finance.category.dto.CategoryDto;
import org.example.finance.category.model.Category;
import org.example.finance.category.repository.CategoryRepository;
import org.example.finance.common.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

@Service
@Transactional
public class CategoryService {
    private final CategoryRepository repository;

    public CategoryService(CategoryRepository repository) {
        this.repository = repository;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDto create(CategoryDto d) {
        return dto(repository.save(new Category(d.name(), d.type(), parent(d.parentId(), d.type(), null))));
    }

    @Transactional(readOnly = true)
    public Page<CategoryDto> list(Pageable p) {
        return repository.findAll(p).map(this::dto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryDto update(UUID id, CategoryDto d) {
        var e = get(id);
        if (e.getType() != d.type()) throw new IllegalStateException("Category type cannot be changed");
        e.update(d.name(), parent(d.parentId(), d.type(), id), d.active());
        return dto(e);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(UUID id) {
        repository.delete(get(id));
    }

    public Category get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Category"));
    }

    private Category parent(UUID id, org.example.finance.category.model.CategoryType type, UUID current) {
        if (id == null) return null;
        if (id.equals(current)) throw new IllegalArgumentException("Category cannot be its own parent");
        var p = get(id);
        if (p.getType() != type) throw new IllegalArgumentException("Parent category type differs");
        for (var cursor = p; cursor != null; cursor = cursor.getParent())
            if (cursor.getId().equals(current)) throw new IllegalArgumentException("Category hierarchy cycle");
        return p;
    }

    private CategoryDto dto(Category e) {
        return new CategoryDto(e.getId(), e.getName(), e.getType(), e.getParent() == null ? null : e.getParent().getId(), e.isActive());
    }
}
