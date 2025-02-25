# Ticketing System API Documentation

## Overview
The Ticketing System API provides role-based access control for managing support tickets. It includes endpoints for user authentication, ticket management, and comments.

## Authentication
All secured endpoints require a cookie for authentication.

## Endpoints

### **1. Login**
#### `POST /login`
Logs in a user.

#### Request Body
```json
{
  "username": "user@example.com",
  "password": "password123"
}
```

#### Responses
- **204 No Content** – Login succeeded
- **401 Unauthorized** – Login failed

A remember-me cookie is set after successful authentication. Make sure to include it in the endpoints that require authorization with `Cookie: remember-me=...`

---
### **2. Ticket Management**

#### `GET /tickets`
Retrieves a list of tickets.

##### Query Parameters
- `status` (optional, enum: `NEW`, `IN_PROGRESS`, `RESOLVED`)
- `id` (optional, long)

##### Responses
- **200 OK** – Returns a paginated list of tickets

#### `POST /tickets`
Creates a new ticket.

##### Request Body
```json
{
  "title": "Issue title",
  "description": "Detailed description",
  "priority": "HIGH",
  "category": "SOFTWARE",
  "status": "NEW"
}
```

##### Responses
- **201 Created** – Ticket successfully created

#### `GET /tickets/{ticketId}`
Retrieves a specific ticket by ID.

##### Path Parameters
- `ticketId` (required, long)

##### Responses
- **200 OK** – Returns ticket details

#### `PATCH /tickets/{ticketId}`
Updates the status of a ticket (IT Support only).

##### Path Parameters
- `ticketId` (required, long)

##### Request Body
```json
{
  "status": "IN_PROGRESS"
}
```

##### Responses
- **200 OK** – Ticket status updated

---
### **3. Comments**

#### `GET /tickets/{ticketId}/comments`
Retrieves comments for a specific ticket.

##### Path Parameters
- `ticketId` (required, long)

##### Responses
- **200 OK** – Returns a paginated list of comments

#### `POST /tickets/{ticketId}/comments`
Adds a comment to a ticket.

##### Path Parameters
- `ticketId` (required, long)

##### Request Body
```json
{
  "text": "This is a comment."
}
```

##### Responses
- **201 Created** – Comment added successfully

#### `PATCH /tickets/{ticketId}/comments/{commentId}`
Updates a comment.

##### Path Parameters
- `ticketId` (required, long)
- `commentId` (required, long)

##### Request Body
```json
{
  "text": "Updated comment text."
}
```

##### Responses
- **200 OK** – Comment updated

#### `GET /tickets/{ticketId}/comments/{commentId}/history`
Retrieves the history of a comment.

##### Path Parameters
- `ticketId` (required, long)
- `commentId` (required, long)

##### Responses
- **200 OK** – Returns the comment history

---
## Data Models

### Ticket
```json
{
  "id": 1,
  "title": "Issue title",
  "description": "Detailed description",
  "priority": "HIGH",
  "category": "SOFTWARE",
  "status": "NEW",
  "created_at": "2023-02-25T12:34:56Z",
  "created_by": { "id": 2, "username": "user1" }
}
```

### Comment
```json
{
  "id": 10,
  "text": "This is a comment.",
  "created_at": "2023-02-25T12:40:00Z",
  "created_by": { "id": 2, "username": "user1" },
  "ticket_id": 1
}
```

## Security
The API uses **Cookie Authentication**. Include a remember-me token (retrieved after successful login) in the `Cookie` header:

```
Cookie: remember-me=<your-token>
```

---
