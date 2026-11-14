package com.farmtrack.farmtrack.exception;

/** Thrown when a unique value (phone, email) is already used. Returned as HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
