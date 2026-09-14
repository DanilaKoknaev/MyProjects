package org.example.finance.counterparty.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Table;
import org.example.finance.common.model.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "counterparties")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Counterparty extends BaseEntity {
    @Column(nullable = false, length = 250)
    private String name;
    @Column(name = "tax_identifier", unique = true, length = 50)
    private String taxIdentifier;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CounterpartyType type;
    @Column(nullable = false)
    private boolean active = true;

    public Counterparty(String name, String taxIdentifier, CounterpartyType type) {
        this.name = name;
        this.taxIdentifier = taxIdentifier;
        this.type = type;
    }

    public void update(String name, String taxIdentifier, CounterpartyType type, boolean active) {
        this.name = name;
        this.taxIdentifier = taxIdentifier;
        this.type = type;
        this.active = active;
    }
}
