package io.hahn.ticketing.tickets.entity;

public enum TicketPriority {
    LOW(0), MEDIUM(50), HIGH(100);

    private final int order;

    TicketPriority(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }
}
