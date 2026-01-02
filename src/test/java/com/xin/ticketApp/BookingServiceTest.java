package com.xin.ticketApp;

import com.xin.ticketApp.model.Ticket;
import com.xin.ticketApp.model.TicketStatus;
import com.xin.ticketApp.repository.TicketRepository;
import com.xin.ticketApp.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = TicketingApplication.class) // Point to your main app
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TicketRepository ticketRepository;

    @BeforeEach
    void setup() {
        // 1. Clean the DB and create a fresh AVAILABLE ticket before every test
        ticketRepository.deleteAll();

        Ticket ticket = new Ticket();
        ticket.setStatus(TicketStatus.AVAILABLE);
        ticket.setVersion(0);
        ticket.setOwnerEmail(null);
        ticketRepository.save(ticket);
    }

    @Test
    void testRaceCondition_OptimisticLocking() throws InterruptedException {
        Long ticketId = ticketRepository.findAll().get(0).getId();

        int numberOfThreads = 2;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        // This latch acts like a "starting gun" for the race
        CountDownLatch latch = new CountDownLatch(1);

        // Thread-safe counters to verify results
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockFailureCount = new AtomicInteger(0);
        AtomicInteger otherExceptionCount = new AtomicInteger(0);

        // Simulate simultaneous booking
        for (int i = 0; i < numberOfThreads; i++) {
            String email = "usera" + i + "@test.com";

            executor.submit(() -> {
                try {
                    // Wait here until the "starting gun" is fired
                    latch.await();

                    bookingService.bookTicket(ticketId, email);
                    successCount.incrementAndGet();

                } catch (ObjectOptimisticLockingFailureException e) {
                    // This is what we WANT to happen for the loser
                    optimisticLockFailureCount.incrementAndGet();
                } catch (Exception e) {
                    // Any other error is a failure of the test code
                    otherExceptionCount.incrementAndGet();
                }
            });
        }

        // Fire the starting gun! Both threads start NOW.
        latch.countDown();

        // Wait for threads to finish
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Exactly one person should have succeeded
        assertEquals(1, successCount.get(), "Expected exactly 1 successful booking");

        // Exactly one person should have failed due to concurrency (Version mismatch)
        assertEquals(1, optimisticLockFailureCount.get(), "Expected exactly 1 Optimistic Locking Failure");

        // No weird crashes (NullPointer, etc)
        assertEquals(0, otherExceptionCount.get(), "Unexpected exceptions occurred");

        // Verify db state
        Ticket finalTicket = ticketRepository.findById(ticketId).orElseThrow();
        assertEquals(TicketStatus.SOLD, finalTicket.getStatus());
        assertEquals(1, finalTicket.getVersion(), "Version should have incremented exactly once");
    }
}