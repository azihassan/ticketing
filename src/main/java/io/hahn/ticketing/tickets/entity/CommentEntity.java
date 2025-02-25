package io.hahn.ticketing.tickets.entity;

import io.hahn.ticketing.users.entity.AccountEntity;
import jakarta.persistence.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Table(name = "reply") //comment is reserved in oracle SQL
@Audited
public class CommentEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    public String text;

    @CreatedDate
    public LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    @Audited(targetAuditMode = NOT_AUDITED)
    public AccountEntity createdBy;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_id")
    @Audited(targetAuditMode = NOT_AUDITED)
    public TicketEntity ticket;
}
