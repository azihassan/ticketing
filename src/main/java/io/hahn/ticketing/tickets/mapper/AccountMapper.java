package io.hahn.ticketing.tickets.mapper;

import io.hahn.ticketing.model.Account;
import io.hahn.ticketing.users.entity.AccountEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountEntity toEntity(Account dto);
    Account toDTO(AccountEntity entity);
}
