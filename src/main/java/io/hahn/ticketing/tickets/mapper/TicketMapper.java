package io.hahn.ticketing.tickets.mapper;

import io.hahn.ticketing.model.Ticket;
import io.hahn.ticketing.model.TicketCreate;
import io.hahn.ticketing.model.TicketPage;
import io.hahn.ticketing.tickets.entity.TicketEntity;
import io.hahn.ticketing.users.mapper.AccountMapper;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = AccountMapper.class)
public interface TicketMapper {
    TicketEntity toEntity(TicketCreate dto);

    Ticket toDTO(TicketEntity createdTicket);
    TicketPage toTicketPage(Page<Ticket> createdTicket);
}
