package io.hahn.ticketing.tickets.mapper;

import io.hahn.ticketing.model.Ticket;
import io.hahn.ticketing.model.TicketCreate;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TicketMapper {
    TicketEntity toEntity(TicketCreate dto);

    Ticket toDTO(TicketEntity createdTicket);
}
