package com.lms.emi.exception;

/**
 * Exception thrown when an installment is not found.
 */
public class InstallmentNotFoundException extends RuntimeException {
    
    public InstallmentNotFoundException(Long installmentId) {
        super("Installment not found with id: " + installmentId);
    }
    
    public InstallmentNotFoundException(String message) {
        super(message);
    }
}
