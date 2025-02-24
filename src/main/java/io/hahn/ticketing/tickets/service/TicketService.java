package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.api.TicketsApiDelegate;
import io.hahn.ticketing.model.Status;
import io.hahn.ticketing.model.Ticket;
import io.hahn.ticketing.model.TicketCreate;
import io.hahn.ticketing.model.TicketPage;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.tickets.mapper.AccountMapper;
import io.hahn.ticketing.tickets.mapper.TicketMapper;
import io.hahn.ticketing.tickets.repository.TicketRepository;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.repository.AccountRepository;
import jakarta.persistence.criteria.Join;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class TicketService implements TicketsApiDelegate {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final AccountRepository accountRepository;

    @Autowired
    public TicketService(TicketRepository repository, TicketMapper mapper, AccountRepository accountRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.accountRepository = accountRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<Ticket> createTicket(TicketCreate ticketCreate) {
        TicketEntity entity = mapper.toEntity(ticketCreate);
        entity.createdBy = getLoggedInAccount();
        TicketEntity createdTicket = repository.save(entity);
        return new ResponseEntity<>(mapper.toDTO(createdTicket), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Ticket> getTicketById(Integer ticketId) {
        Optional<TicketEntity> ticket = repository.findById(Long.valueOf(ticketId));
        verifyOwnership(ticket);
        return ResponseEntity.of(ticket.map(mapper::toDTO));
    }

    private void verifyOwnership(Optional<TicketEntity> ticket) {
        ticket.ifPresent(t -> {
            Authentication account = SecurityContextHolder.getContext().getAuthentication();
            boolean isEmployee = !account.getAuthorities().contains(new SimpleGrantedAuthority("IT"));
            if(isEmployee && !account.getName().equals(t.createdBy.username)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        });
    }

    @Override
    public ResponseEntity<TicketPage> listTickets(String status, Long id, final Pageable pageable) {
        Authentication account = SecurityContextHolder.getContext().getAuthentication();
        Specification<TicketEntity> filters = TicketSpecification.byID(id).and(TicketSpecification.byStatus(status));
        if(!account.getAuthorities().contains(new SimpleGrantedAuthority("IT"))) {
            filters = filters.and(TicketSpecification.byUsername(account.getName()));
        }
        Page<Ticket> tickets = repository.findAll(filters, pageable).map(mapper::toDTO);
        return ResponseEntity.ok(mapper.toTicketPage(tickets));
    }

    private AccountEntity getLoggedInAccount() {
        Authentication account = SecurityContextHolder.getContext().getAuthentication();
        return accountRepository.findByUsername(account.getName()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
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

    public static Specification<TicketEntity> byUsername(String username) {
        return ((root, query, builder) -> {
            Join<TicketEntity, AccountEntity> accountJoin = root.join("createdBy");
            return builder.equal(accountJoin.<Long>get("username"), username);
        });
    }
}