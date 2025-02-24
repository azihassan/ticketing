package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.api.TicketsApiDelegate;
import io.hahn.ticketing.model.Status;
import io.hahn.ticketing.model.Ticket;
import io.hahn.ticketing.model.TicketCreate;
import io.hahn.ticketing.model.TicketPage;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.tickets.mapper.TicketMapper;
import io.hahn.ticketing.tickets.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Service
public class TicketService implements TicketsApiDelegate {

    private final TicketRepository repository;
    private final TicketMapper mapper;

    @Autowired
    public TicketService(TicketRepository repository, TicketMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<Ticket> createTicket(TicketCreate ticketCreate) {
        TicketEntity createdTicket = repository.save(mapper.toEntity(ticketCreate));
        return new ResponseEntity<>(mapper.toDTO(createdTicket), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Ticket> getTicketById(Integer ticketId) {
        Optional<TicketEntity> ticket = repository.findById(Long.valueOf(ticketId));
        return ResponseEntity.of(ticket.map(mapper::toDTO));
    }

    @Override
    public ResponseEntity<TicketPage> listTickets(String status, Long id, final Pageable pageable) {
        Specification<TicketEntity> filters = TicketSpecification.byID(id).and(TicketSpecification.byStatus(status));
        Page<Ticket> tickets = repository.findAll(filters, pageable).map(mapper::toDTO);
        return ResponseEntity.ok(mapper.toTicketPage(tickets));
    }
}

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
}