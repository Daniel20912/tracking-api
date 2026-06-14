package com.danieloliveira.tracking.exception;

public class TrackingCodeAlreadyExistsException extends RuntimeException {
    public TrackingCodeAlreadyExistsException(String message) {
        super(message);
    }
}
