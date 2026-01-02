package com.example.jackpot.exception;

/**
 * Thrown when a jackpot is not found by id.
 */
public class JackpotNotFoundException extends RuntimeException {
    public JackpotNotFoundException(String message) {
        super(message);
    }
}
