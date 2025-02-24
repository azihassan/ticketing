package io.hahn.ticketing.users.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "role")
public class RoleEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public String role;

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "roles")
    public Set<AccountEntity> accounts;

    @CreatedDate
    public LocalDateTime createdAt = LocalDateTime.now();
}
