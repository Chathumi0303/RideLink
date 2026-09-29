package com.ridelink.ridemanagement.exception;

public class UnauthorizedRideAccessException extends RuntimeException {
    public UnauthorizedRideAccessException(String message) {
        super(message);
    }
}
