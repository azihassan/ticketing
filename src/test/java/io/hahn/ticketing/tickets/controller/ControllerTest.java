package io.hahn.ticketing.tickets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hahn.ticketing.users.entity.UserWithID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@AutoConfigureMockMvc
@SpringBootTest
public abstract class ControllerTest {
    private final List<SimpleGrantedAuthority> EMPLOYEE_AUTHORITY = List.of(new SimpleGrantedAuthority("EMPLOYEE"));
    private final List<SimpleGrantedAuthority> IT_AUTHORITY = List.of(new SimpleGrantedAuthority("IT"));

    protected final String EMPLOYEE_1_NAME = "employee_demo";
    protected final String EMPLOYEE_2_NAME = "employee_demo_2";
    protected final String IT_1_NAME = "it_demo";

    // custom principal mocking, courtesy of https://stackoverflow.com/a/57093952/3729391
    protected final RequestPostProcessor mockEmployeeAccount = user(new UserWithID(EMPLOYEE_1_NAME, "", EMPLOYEE_AUTHORITY, 1L));
    protected final RequestPostProcessor mockITAccount = user(new UserWithID(IT_1_NAME, "", IT_AUTHORITY, 2L));
    protected final RequestPostProcessor mockAnotherEmployeeAccount = user(new UserWithID(EMPLOYEE_2_NAME, "", EMPLOYEE_AUTHORITY, 3L));

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;
}
