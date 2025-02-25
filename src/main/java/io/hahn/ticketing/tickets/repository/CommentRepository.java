package io.hahn.ticketing.tickets.repository;

import io.hahn.ticketing.tickets.entity.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentRepository extends PagingAndSortingRepository<CommentEntity, Long>, JpaRepository<CommentEntity, Long> {
    Page<CommentEntity> findByTicketId(Long ticketID, Pageable pageable);
    Page<CommentEntity> findByTicketIdAndTicketCreatedByUsername(Long ticketID, String username, Pageable pageable);

    Optional<CommentEntity> findByIdAndCreatedByUsername(Long commentId, String username);
}
