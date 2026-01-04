package com.lms.loan.exception;

/**
 * Exception thrown when loan eligibility validation fails.
 */
public class LoanEligibilityException extends RuntimeException {
    
    public LoanEligibilityException(String message) {
        super(message);
    }
    
    public LoanEligibilityException(String message, Throwable cause) {
        super(message, cause);
    }
}
