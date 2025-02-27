package io.hahn.ticketing.users.mapper;

import io.hahn.ticketing.model.Account;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.entity.RoleEntity;
import io.hahn.ticketing.users.entity.UserWithID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(target = "roles", source = "authorities")
    AccountEntity toEntity(UserWithID user, Collection<? extends GrantedAuthority> authorities);
    Account toDTO(AccountEntity entity);

    @Mapping(source = "authority", target = "role")
    RoleEntity toEntity(GrantedAuthority authority);
}
