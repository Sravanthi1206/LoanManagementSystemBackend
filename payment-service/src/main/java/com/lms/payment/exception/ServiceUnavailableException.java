package com.lms.payment.exception;

/**
 * Exception thrown when an external service is unavailable (circuit breaker open).
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message) {
        super(message);
    }

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
