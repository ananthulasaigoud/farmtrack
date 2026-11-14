package com.farmtrack.farmtrack.exception;

/** Business rule violation (e.g. harvest date before sowing date). Returned as HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
