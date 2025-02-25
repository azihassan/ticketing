package io.hahn.ticketing.tickets.controller;

import io.hahn.ticketing.model.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.UserRequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TicketControllerTest extends ControllerTest {

    @Test
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
        String response = mvc.perform(post("/tickets").content(json).contentType(MediaType.APPLICATION_JSON).with(mockEmployeeAccount))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Ticket createdTicket = objectMapper.readValue(response, Ticket.class);

        mvc.perform(get("/tickets/" + createdTicket.getId()).with(mockEmployeeAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(ticket.getTitle()))
                .andExpect(jsonPath("$.description").value(ticket.getDescription()))
                .andExpect(jsonPath("$.priority").value(ticket.getPriority().getValue()))
                .andExpect(jsonPath("$.category").value(ticket.getCategory().getValue()))
                .andExpect(jsonPath("$.status").value(ticket.getStatus().getValue()))
                .andExpect(jsonPath("$.created_by.username").value("employee_demo"));
    }

    @Test
    @Transactional
    public void whenUserIsNotEmployee_shouldNotCreateTicket() throws Exception {
        String json = objectMapper.writeValueAsString(new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        ));
        mvc.perform(post("/tickets").content(json).contentType(MediaType.APPLICATION_JSON).with(mockITAccount))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void shouldListTickets() throws Exception {
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
                    .andExpect(status().isCreated());
        }

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
        ).andExpect(status().isCreated());

        ticket.setStatus(Status.RESOLVED);
        mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(ticket))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockAnotherEmployeeAccount)
        ).andExpect(status().isCreated());

        UserRequestPostProcessor mockITAccount = user("it_demo").authorities(new SimpleGrantedAuthority("IT"));
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

    @Test
    @Transactional
    public void whenUserIsEmployee_shouldListOwnTickets() throws Exception {
        TicketCreate employeeTicket = new TicketCreate().title("Ticket of employee #1");
        mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(employeeTicket))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockEmployeeAccount)
        ).andExpect(status().isCreated());

        TicketCreate anotherEmployeeTicket = new TicketCreate().title("Ticket of employee #2");
        mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(anotherEmployeeTicket))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockAnotherEmployeeAccount)
        ).andExpect(status().isCreated());

        mvc.perform(get("/tickets?page=0&size=20").with(mockEmployeeAccount))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value(employeeTicket.getTitle()));

        mvc.perform(get("/tickets?page=0&size=20").with(mockAnotherEmployeeAccount))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value(anotherEmployeeTicket.getTitle()));
    }

    @Test
    @Transactional
    public void whenUserIsEmployee_shouldViewOwnTicket() throws Exception {
        Ticket employeeTicket = objectMapper.readValue(mvc.perform(post("/tickets")
                .content(objectMapper.writeValueAsString(new TicketCreate().title("Ticket of employee #1")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockEmployeeAccount)
        ).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(), Ticket.class);


        mvc.perform(get("/tickets/" + employeeTicket.getId()).with(mockEmployeeAccount))
                .andExpect(status().isOk());

        mvc.perform(get("/tickets/" + employeeTicket.getId()).with(mockAnotherEmployeeAccount))
                .andExpect(status().isForbidden());
    }
}
