package com.xin.ticketApp.controller;

import com.xin.ticketApp.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tickets")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/{id}/book")
    public ResponseEntity<String> bookTicket(@PathVariable Long id, @RequestParam String email) {
        bookingService.bookTicket(id, email);
        return ResponseEntity.ok("Ticket #" + id + " successfully booked for " + email);
    }
}