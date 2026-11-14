package com.farmtrack.farmtrack.exception;

/** Thrown when a requested record does not exist. Returned to the client as HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
