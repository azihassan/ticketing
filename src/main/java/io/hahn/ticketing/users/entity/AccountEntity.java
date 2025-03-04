package io.hahn.ticketing.users.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "account")
public class AccountEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public String username;
    @Column(name = "passwd")
    public String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "account_role",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    public Set<RoleEntity> roles;

    @CreatedDate
    public LocalDateTime createdAt = LocalDateTime.now();
}
