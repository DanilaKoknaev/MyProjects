package org.example.finance.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.example.finance.common.model.BaseEntity;
import org.example.finance.department.model.Department;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Column(nullable = false, unique = true, length = 320)
    private String email;
    @Column(name = "password_hash")
    private String passwordHash;
    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;
    @Column(nullable = false)
    private boolean enabled = true;

    public User(String email, String passwordHash, String fullName, Role role, Department department) {
        this.email = email.toLowerCase();
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.department = department;
    }

    public void update(String fullName, Role role, Department department, boolean enabled) {
        this.fullName = fullName;
        this.role = role;
        this.department = department;
        this.enabled = enabled;
    }
}
