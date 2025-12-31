package com.lms.identity.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when an invalid role is specified.
 * For example, trying to create a CUSTOMER via admin endpoint.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRoleException extends RuntimeException {
    
    public InvalidRoleException(String message) {
        super(message);
    }
}
