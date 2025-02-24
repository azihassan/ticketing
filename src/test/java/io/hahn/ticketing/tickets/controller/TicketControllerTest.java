package io.hahn.ticketing.tickets.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hahn.ticketing.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
}
