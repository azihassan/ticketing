package io.hahn.ticketing.tickets.mapper;

import io.hahn.ticketing.model.Comment;
import io.hahn.ticketing.model.CommentCreate;
import io.hahn.ticketing.model.CommentPage;
import io.hahn.ticketing.tickets.entity.CommentEntity;
import io.hahn.ticketing.users.mapper.AccountMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = { AccountMapper.class, TicketMapper.class })
public interface CommentMapper {
    @Mapping(source = "ticketID", target = "ticket.id")
    CommentEntity toEntity(CommentCreate dto, Long ticketID);

    @Mapping(source = "ticket.id", target = "ticketId")
    Comment toDTO(CommentEntity createdComment);
    CommentPage toCommentPage(Page<Comment> createdComment);
}
