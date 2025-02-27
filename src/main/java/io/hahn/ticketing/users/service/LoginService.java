package io.hahn.ticketing.users.service;

import io.hahn.ticketing.api.LoginApiDelegate;
import io.hahn.ticketing.model.Account;
import io.hahn.ticketing.model.Login;
import io.hahn.ticketing.users.entity.UserWithID;
import io.hahn.ticketing.users.mapper.AccountMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements LoginApiDelegate {
    private final AccountMapper accountMapper;
    private final AuthenticationManager authenticationManager;
    private final RememberMeServices rememberMeServices;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public LoginService(AccountMapper accountMapper, AuthenticationManager authenticationManager, RememberMeServices rememberMeServices, HttpServletRequest request, HttpServletResponse response) {
        this.accountMapper = accountMapper;
        this.authenticationManager = authenticationManager;
        this.rememberMeServices = rememberMeServices;
        this.request = request;
        this.response = response;
    }

    @Override
    public ResponseEntity<Account> login(Login dto) {
        Authentication authentication = authenticationManager.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.getUsername(), dto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        rememberMeServices.loginSuccess(request, response, authentication);
        return ResponseEntity.ok(accountMapper.toDTO(accountMapper.toEntity((UserWithID) authentication.getPrincipal(), authentication.getAuthorities())));
    }
}
