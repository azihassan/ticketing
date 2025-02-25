package io.hahn.ticketing.tickets.controller;

import io.hahn.ticketing.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentAuditControllerTest extends ControllerTest {

    //@Test
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
