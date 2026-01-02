package com.xin.ticketApp.service;

import com.xin.ticketApp.model.Ticket;
import com.xin.ticketApp.model.TicketStatus;
import com.xin.ticketApp.repository.TicketRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BookingService {

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public Ticket bookTicket(Long ticketId, String email) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found with ID: " + ticketId));

        log.info("Attempting to book ticket {} for user {}", ticketId, email);

        if (ticket.getStatus() != TicketStatus.AVAILABLE) {
            throw new RuntimeException("Ticket " + ticketId + " is already " + ticket.getStatus());
        }

        // Simulate "Work" or Latency
        // This makes it easier to test concurrency manually later
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        ticket.setStatus(TicketStatus.SOLD);
        ticket.setOwnerEmail(email);
        log.info("Successfully booked ticket {} for {}", ticketId, email);

        // Hibernate will check the 'version' here.
        // If it changed in the DB since findById from above, it throws
        // ObjectOptimisticLockingFailureException
        return ticketRepository.save(ticket);
    }
}