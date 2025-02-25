package io.hahn.ticketing.tickets.entity;

import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import jakarta.persistence.*;

@Entity
@RevisionEntity
@Table(name = "revinfo")
public class Revision {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "REV")
    @RevisionNumber
    private long rev;

    @Column(name = "revtstmp")
    @RevisionTimestamp
    private long timestamp;

    public long getRev() {
        return rev;
    }

    public void setRev(long rev) {
        this.rev = rev;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
