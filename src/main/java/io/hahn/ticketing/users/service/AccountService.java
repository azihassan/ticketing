package io.hahn.ticketing.users.service;

import io.hahn.ticketing.api.MeApiDelegate;
import io.hahn.ticketing.model.Account;
import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.entity.UserWithID;
import io.hahn.ticketing.users.mapper.AccountMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AccountService implements MeApiDelegate {

    private final AccountMapper accountMapper;

    public AccountService(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    @Override
    public ResponseEntity<Account> getMe() {
        return ResponseEntity.ok(accountMapper.toDTO(getLoggedInAccount()));
    }


    public AccountEntity getLoggedInAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return accountMapper.toEntity((UserWithID) authentication.getPrincipal(), authentication.getAuthorities());
    }
}
