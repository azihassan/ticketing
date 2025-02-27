package io.hahn.ticketing.tickets.service;

import io.hahn.ticketing.api.TicketsApiDelegate;
import io.hahn.ticketing.model.*;
import io.hahn.ticketing.tickets.entity.CommentEntity;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.tickets.mapper.CommentMapper;
import io.hahn.ticketing.tickets.mapper.TicketMapper;
import io.hahn.ticketing.tickets.repository.CommentRepository;
import io.hahn.ticketing.tickets.repository.TicketRepository;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.repository.AccountRepository;
import io.hahn.ticketing.users.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TicketService implements TicketsApiDelegate {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final AccountService accountService;
    private final CommentMapper commentMapper;
    private final CommentRepository commentRepository;

    @Autowired
    public TicketService(TicketRepository repository, TicketMapper mapper, AccountService accountService, AccountRepository accountRepository, CommentMapper commentMapper, CommentRepository commentRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.accountService = accountService;
        this.commentMapper = commentMapper;
        this.commentRepository = commentRepository;
    }

    @Override
    @PreAuthorize("hasAuthority('EMPLOYEE')")
    public ResponseEntity<Ticket> createTicket(TicketCreate ticketCreate) {
        TicketEntity entity = mapper.toEntity(ticketCreate);
        entity.createdBy = accountService.getLoggedInAccount();
        TicketEntity createdTicket = repository.save(entity);
        return new ResponseEntity<>(mapper.toDTO(createdTicket), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<List<Comment>> getCommentHistory(Long ticketId, Long commentId) {
        List<Comment> revisions = commentRepository.findRevisions(commentId)
                .stream()
                .map(revision -> commentMapper.toDTO(revision.getEntity()))
                .collect(Collectors.toList());
        Collections.reverse(revisions);
        return ResponseEntity.ok(revisions);
    }

    @Override
    public ResponseEntity<Ticket> getTicketById(Long ticketId) {
        Optional<TicketEntity> ticket = repository.findById(ticketId);
        ticket.ifPresent(this::verifyOwnership);
        return ResponseEntity.of(ticket.map(mapper::toDTO));
    }

    private void verifyOwnership(TicketEntity ticket) {
        AccountEntity account = accountService.getLoggedInAccount();
        boolean isEmployee = !account.roles.stream().map(role -> role.role).toList().contains("IT");
        if(isEmployee && !account.username.equals(ticket.createdBy.username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    @Override
    public ResponseEntity<TicketPage> listTickets(String status, Long id, final Pageable pageable) {
        AccountEntity account = accountService.getLoggedInAccount();
        Specification<TicketEntity> filters = TicketSpecification.byID(id).and(TicketSpecification.byStatus(status));
        if(!account.roles.stream().map(role -> role.role).toList().contains("IT")) {
            filters = filters.and(TicketSpecification.byUsername(account.username));
        }
        Page<Ticket> tickets = repository.findAll(filters, pageable).map(mapper::toDTO);
        return ResponseEntity.ok(mapper.toTicketPage(tickets));
    }

    @Override
    @PreAuthorize("hasAuthority('IT')")
    public ResponseEntity<Comment> updateComment(Long ticketId, Long commentId, UpdateCommentRequest updateCommentRequest) {
        return ResponseEntity.of(commentRepository.findByIdAndCreatedByUsername(commentId, accountService.getLoggedInAccount().username).map(comment -> {
            comment.text = updateCommentRequest.getText();
            commentRepository.save(comment);
            return commentMapper.toDTO(comment);
        }));
    }

    @Override
    @PreAuthorize("hasAuthority('IT')")
    public ResponseEntity<Ticket> updateTicketStatus(Long ticketId, UpdateTicketStatusRequest updateTicketStatusRequest) {
        return ResponseEntity.of(repository.findById(ticketId).map(ticket -> {
            ticket.status = mapper.toEntity(updateTicketStatusRequest.getStatus());
            repository.save(ticket);
            return mapper.toDTO(ticket);
        }));
    }

    @Override
    @PreAuthorize("hasAuthority('IT')")
    public ResponseEntity<Comment> addComment(Long ticketId, CommentCreate commentCreate) {
        CommentEntity entity = commentMapper.toEntity(commentCreate, ticketId);
        entity.createdBy = accountService.getLoggedInAccount();
        CommentEntity createdComment = commentRepository.save(entity);
        return new ResponseEntity<>(commentMapper.toDTO(createdComment), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CommentPage> listComments(Long ticketId, final Pageable pageable) {
        AccountEntity account = accountService.getLoggedInAccount();
        boolean isIT = account.roles.stream().map(role -> role.role).toList().contains("IT");

        if(isIT) {
            Page<Comment> comments = commentRepository.findByTicketId(ticketId, pageable).map(commentMapper::toDTO);
            return ResponseEntity.ok(commentMapper.toCommentPage(comments));
        }
        Page<Comment> comments = commentRepository.findByTicketIdAndTicketCreatedByUsername(ticketId, account.username, pageable).map(commentMapper::toDTO);
        return ResponseEntity.ok(commentMapper.toCommentPage(comments));
    }
}