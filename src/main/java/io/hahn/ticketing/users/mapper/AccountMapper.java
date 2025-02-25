package io.hahn.ticketing.users.mapper;

import io.hahn.ticketing.model.Account;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.entity.UserWithID;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountEntity toEntity(UserWithID user);
    Account toDTO(AccountEntity entity);
}
