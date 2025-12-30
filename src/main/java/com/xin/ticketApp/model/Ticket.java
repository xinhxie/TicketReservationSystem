package com.xin.ticketApp.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tickets")
@Data
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id")
    private Integer eventId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ticket_status")
    private TicketStatus status;

    @Version // For concurrency
    private Integer version;

    @Column(name = "owner_email")
    private String ownerEmail;
}