package io.hahn.ticketing.users.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

//UserWithID in case ID is needed in foreign key related scenarios
//eg. findTicketsByAccountId(currentUserWithID.getId())
//tip from https://stackoverflow.com/q/22678891/3729391
public class UserWithID extends User {

    private final Long id;

    public UserWithID(String username, String password, Collection<? extends GrantedAuthority> authorities, Long id) {
        super(username, password, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
