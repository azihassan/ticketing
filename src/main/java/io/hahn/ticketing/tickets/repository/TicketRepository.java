package io.hahn.ticketing.tickets.repository;

import io.hahn.ticketing.tickets.entity.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends PagingAndSortingRepository<TicketEntity, Long>, JpaRepository<TicketEntity, Long> {
}
