package io.hahn.ticketing.users.service;

import io.hahn.ticketing.users.entity.AccountEntity;
import io.hahn.ticketing.users.entity.UserWithID;
import io.hahn.ticketing.users.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AccountDetailsService implements UserDetailsService {
    private final AccountRepository repository;

    @Autowired
    public AccountDetailsService(AccountRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AccountEntity user = repository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User " + username + " doesn't exis"));
        return new UserWithID(user.username, user.password, user.roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.role))
                .collect(Collectors.toList()), user.id);
    }
}

