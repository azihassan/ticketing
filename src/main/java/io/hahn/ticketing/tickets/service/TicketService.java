package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.api.TicketsApiDelegate;
import io.hahn.ticketing.model.Ticket;
import io.hahn.ticketing.model.TicketCreate;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.tickets.mapper.TicketMapper;
import io.hahn.ticketing.tickets.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public ResponseEntity<Ticket> createTicket(TicketCreate ticketCreate) {
        TicketEntity createdTicket = repository.save(mapper.toEntity(ticketCreate));
        return new ResponseEntity<Ticket>(mapper.toDTO(createdTicket), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Ticket> getTicketById(Integer ticketId) {
        Optional<TicketEntity> ticket = repository.findById(Long.valueOf(ticketId));
        return ResponseEntity.of(ticket.map(mapper::toDTO));
    }
}
