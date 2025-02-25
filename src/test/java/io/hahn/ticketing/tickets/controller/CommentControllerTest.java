package io.hahn.ticketing.tickets.controller;

import io.hahn.ticketing.model.*;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest extends ControllerTest {

    @Test
    @Transactional
    public void whenUserIsEmployee_shouldNotCreateComment() throws Exception {
        TicketCreate ticket = new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );

        Ticket createdTicket = objectMapper.readValue(
                mvc.perform(post("/tickets")
                                .content(objectMapper.writeValueAsString(ticket))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(mockEmployeeAccount)
                        ).andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Ticket.class);

        Comment comment = new Comment().text("Hello there");
        mvc.perform(post("/tickets/" + createdTicket.getId() + "/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockEmployeeAccount)
        ).andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    public void whenUserIsIT_shouldCreateComment() throws Exception {
        TicketCreate ticket = new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );

        Ticket createdTicket = objectMapper.readValue(
                mvc.perform(post("/tickets")
                                .content(objectMapper.writeValueAsString(ticket))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(mockEmployeeAccount)
                ).andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
        Ticket.class);

        Comment comment = new Comment().text("Hello there");
        mvc.perform(post("/tickets/" + createdTicket.getId() + "/comments")
                .content(objectMapper.writeValueAsString(comment))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockITAccount)
        ).andExpect(status().isCreated());

        mvc.perform(get("/tickets/" + createdTicket.getId() + "/comments").with(mockITAccount))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].text").value(comment.getText()))
                .andExpect(jsonPath("$.content[0].ticket_id").value(createdTicket.getId()))
                .andExpect(jsonPath("$.content[0].created_by.username").value(IT_1_NAME));
    }

    @Test
    @Transactional
    public void whenUserIsEmployee_shouldViewOwnTicketComments() throws Exception {
        TicketCreate ticket = new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );

        Ticket firstEmployeeTicket = objectMapper.readValue(
                mvc.perform(post("/tickets")
                                .content(objectMapper.writeValueAsString(ticket))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(mockEmployeeAccount)
                        ).andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Ticket.class);

        mvc.perform(post("/tickets/" + firstEmployeeTicket.getId() + "/comments")
                .content(objectMapper.writeValueAsString(new Comment().text("Hello there")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockITAccount)
        ).andExpect(status().isCreated());

        mvc.perform(post("/tickets/" + firstEmployeeTicket.getId() + "/comments")
                .content(objectMapper.writeValueAsString(new Comment().text("Hello there 2")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockITAccount)
        ).andExpect(status().isCreated());

        mvc.perform(get("/tickets/" + firstEmployeeTicket.getId() + "/comments")
                .with(mockEmployeeAccount)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/tickets/" + firstEmployeeTicket.getId() + "/comments")
                .with(mockAnotherEmployeeAccount)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    //@Transactional todo: find workaround with envers messing with transactional tests
    public void whenCommentIsUpdated_shouldStoreHistory() throws Exception {
        TicketCreate ticket = new TicketCreate(
                "Ticket 1",
                "New ticket",
                Priority.MEDIUM,
                Category.HARDWARE,
                Status.IN_PROGRESS
        );

        Ticket firstEmployeeTicket = objectMapper.readValue(
                mvc.perform(post("/tickets")
                                .content(objectMapper.writeValueAsString(ticket))
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(mockEmployeeAccount)
                        ).andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Ticket.class);

        Comment createdComment = objectMapper.readValue(
                mvc.perform(post("/tickets/" + firstEmployeeTicket.getId() + "/comments")
                        .content(objectMapper.writeValueAsString(new Comment().text("Hello there")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(mockITAccount)
                ).andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                Comment.class);

        Comment updatedComment = new Comment().text("Hello there (updated)");
        mvc.perform(patch("/tickets/" + firstEmployeeTicket.getId() + "/comments/" + createdComment.getId())
                .content(objectMapper.writeValueAsString(updatedComment))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockITAccount)
        ).andExpect(status().isOk()).andExpect(jsonPath("$.text").value(updatedComment.getText()));

        mvc.perform(patch("/tickets/" + firstEmployeeTicket.getId() + "/comments/" + createdComment.getId())
                .content(objectMapper.writeValueAsString(updatedComment.text("Hello there (updated again)")))
                .contentType(MediaType.APPLICATION_JSON)
                .with(mockITAccount)
        ).andExpect(status().isOk());

        mvc.perform(get("/tickets/" + firstEmployeeTicket.getId() + "/comments/" + createdComment.getId() + "/history")
                .with(mockITAccount)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value(updatedComment.getText()))
                .andExpect(jsonPath("$[1].text").value("Hello there (updated)"))
                .andExpect(jsonPath("$[2].text").value("Hello there"));
    }
}
