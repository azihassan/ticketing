package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.model.Status;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.users.entity.AccountEntity;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

class TicketSpecification {
    public static Specification<TicketEntity> byStatus(String status) {
        return ((root, query, builder) -> {
            return ObjectUtils.isEmpty(status) ? builder.conjunction() : builder.equal(root.<Status>get("status"), status);
        });
    }

    public static Specification<TicketEntity> byID(Long id) {
        return ((root, query, builder) -> {
            return id == null ? builder.conjunction() : builder.equal(root.<Long>get("id"), id);
        });
    }

    public static Specification<TicketEntity> byUsername(String username) {
        return ((root, query, builder) -> {
            Join<TicketEntity, AccountEntity> accountJoin = root.join("createdBy");
            return builder.equal(accountJoin.<Long>get("username"), username);
        });
    }
}
