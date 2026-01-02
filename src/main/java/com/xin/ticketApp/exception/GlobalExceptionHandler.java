package com.xin.ticketApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleConflict(ObjectOptimisticLockingFailureException ex) {
        // Log this as a conflict, not a server crash
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("The ticket was updated by another user. Please refresh and try again.");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}