package io.hahn.ticketing.tickets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hahn.ticketing.model.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
public class TicketControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = { "EMPLOYEE" })
    @Transactional
    public void whenUserIsEmployee_shouldCreateTicket() throws Exception {
        TicketCreate ticket = new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );
        String json = objectMapper.writeValueAsString(ticket);
        String response = mvc.perform(post("/tickets").content(json).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Ticket createdTicket = objectMapper.readValue(response, Ticket.class);

        mvc.perform(get("/tickets/" + createdTicket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(ticket.getTitle()))
                .andExpect(jsonPath("$.description").value(ticket.getDescription()))
                .andExpect(jsonPath("$.priority").value(ticket.getPriority().getValue()))
                .andExpect(jsonPath("$.category").value(ticket.getCategory().getValue()))
                .andExpect(jsonPath("$.status").value(ticket.getStatus().getValue()));
    }

    @Test
    @WithMockUser(authorities = { "IT" })
    @Transactional
    public void whenUserIsNotEmployee_shouldNotCreateTicket() throws Exception {
        String json = objectMapper.writeValueAsString(new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        ));
        mvc.perform(post("/tickets").content(json).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldListTickets() throws Exception {
        UserRequestPostProcessor mockEmployeeAccount = user("employee").authorities(new SimpleGrantedAuthority("EMPLOYEE"));
        for(int t = 0; t < 20; t++) {
            TicketCreate ticket = new TicketCreate(
                    "Ticket " + t,
                    "New ticket",
                    Priority.MEDIUM,
                    Category.HARDWARE,
                    Status.IN_PROGRESS
            );
            String json = objectMapper.writeValueAsString(ticket);
            mvc.perform(post("/tickets").content(json).contentType(MediaType.APPLICATION_JSON).with(mockEmployeeAccount))
                    .andDo(print())
                    .andExpect(status().isCreated());
        }

        UserRequestPostProcessor mockITAccount = user("it").authorities(new SimpleGrantedAuthority("IT"));
        mvc.perform(get("/tickets?page=0&size=10").with(mockITAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(20))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Ticket 0"))
                .andExpect(jsonPath("$.content[3].title").value("Ticket 3"))
                .andExpect(jsonPath("$.content[9].title").value("Ticket 9"));

        mvc.perform(get("/tickets?page=1&size=10").with(mockITAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Ticket 10"));
    }

    @Test
    @Transactional
    public void shouldFilterTickets() throws Exception {
        UserRequestPostProcessor mockEmployeeAccount = user("employee").authorities(new SimpleGrantedAuthority("EMPLOYEE"));
        TicketCreate ticket = new TicketCreate(
                "Ticket",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );
        mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(ticket))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockEmployeeAccount)
        ).andDo(print()).andExpect(status().isCreated());

        UserRequestPostProcessor mockAnotherEmployeeAccount = user("employee 2").authorities(new SimpleGrantedAuthority("EMPLOYEE"));
        ticket.setStatus(Status.RESOLVED);
        mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(ticket))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockAnotherEmployeeAccount)
        ).andExpect(status().isCreated());

        UserRequestPostProcessor mockITAccount = user("it").authorities(new SimpleGrantedAuthority("IT"));
        mvc.perform(get("/tickets?page=0&size=20&status=IN_PROGRESS").with(mockITAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(get("/tickets?page=0&size=20&status=RESOLVED").with(mockITAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));

        mvc.perform(get("/tickets?page=0&size=20&status=NEW").with(mockITAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
