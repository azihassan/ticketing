package io.hahn.ticketing.tickets.entity;

import io.hahn.ticketing.users.entity.AccountEntity;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket")
public class TicketEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public String title;
    public String description;

    @Enumerated(EnumType.STRING)
    public TicketCategory category;

    @Enumerated(EnumType.ORDINAL)
    public TicketPriority priority;

    @Enumerated(EnumType.STRING)
    public TicketStatus status;

    @CreatedDate
    public LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    public AccountEntity createdBy;
}
