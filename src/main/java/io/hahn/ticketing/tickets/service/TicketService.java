package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.api.TicketsApiDelegate;
import io.hahn.ticketing.model.*;
import io.hahn.ticketing.tickets.entity.CommentEntity;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.users.mapper.AccountMapper;
import io.hahn.ticketing.tickets.mapper.CommentMapper;
import io.hahn.ticketing.tickets.mapper.TicketMapper;
import io.hahn.ticketing.tickets.repository.CommentRepository;
import io.hahn.ticketing.tickets.repository.TicketRepository;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.entity.UserWithID;
import io.hahn.ticketing.users.repository.AccountRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class TicketService implements TicketsApiDelegate {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final AccountMapper accountMapper;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Autowired
    public TicketService(TicketRepository repository, TicketMapper mapper, AccountMapper accountMapper, AccountRepository accountRepository, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.accountMapper = accountMapper;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
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
        ticket.ifPresent(this::verifyOwnership);
        return ResponseEntity.of(ticket.map(mapper::toDTO));
    }

    private void verifyOwnership(TicketEntity ticket) {
        Authentication account = SecurityContextHolder.getContext().getAuthentication();
        boolean isEmployee = !account.getAuthorities().contains(new SimpleGrantedAuthority("IT"));
        if(isEmployee && !account.getName().equals(ticket.createdBy.username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
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

    @Override
    @PreAuthorize("hasAuthority('IT')")
    public ResponseEntity<Comment> addComment(Long ticketId, CommentCreate commentCreate) {
        CommentEntity entity = commentMapper.toEntity(commentCreate, ticketId);
        entity.createdBy = getLoggedInAccount();
        CommentEntity createdComment = commentRepository.save(entity);
        return new ResponseEntity<>(commentMapper.toDTO(createdComment), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CommentPage> listComments(Long ticketId, final Pageable pageable) {
        Authentication account = SecurityContextHolder.getContext().getAuthentication();
        boolean isIT = account.getAuthorities().contains(new SimpleGrantedAuthority("IT"));

        if(isIT) {
            Page<Comment> comments = commentRepository.findByTicketId(ticketId, pageable).map(commentMapper::toDTO);
            return ResponseEntity.ok(commentMapper.toCommentPage(comments));
        }
        Page<Comment> comments = commentRepository.findByTicketIdAndTicketCreatedByUsername(ticketId, account.getName(), pageable).map(commentMapper::toDTO);
        return ResponseEntity.ok(commentMapper.toCommentPage(comments));
    }

    private AccountEntity getLoggedInAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountMapper.toEntity((UserWithID) authentication.getPrincipal());
    }
}