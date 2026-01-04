package com.lms.loan.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Global Exception Handler Tests")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Handle LoanNotFoundException")
    void handleLoanNotFoundException() {
        LoanNotFoundException ex = new LoanNotFoundException(1L);
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(anyBoolean())).thenReturn("uri=/test");
        
        ResponseEntity<Map<String, Object>> response = handler.handleLoanNotFoundException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Loan not found with id: 1", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Handle InvalidLoanStatusException")
    void handleInvalidLoanStatusException() {
        InvalidLoanStatusException ex = new InvalidLoanStatusException("Status invalid");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(anyBoolean())).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidLoanStatusException(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Status invalid", response.getBody().get("message"));
    }
    
    @Test
    @DisplayName("Handle RuntimeException")
    void handleRuntimeException() {
        RuntimeException ex = new RuntimeException("Internal Error");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(anyBoolean())).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex, request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal Error", response.getBody().get("message"));
    }
    
    @Test
    @DisplayName("Handle UnauthorizedAccessException")
    void handleUnauthorizedAccessException() {
        UnauthorizedAccessException ex = new UnauthorizedAccessException("Access Denied");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(anyBoolean())).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleUnauthorizedAccessException(ex, request);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", response.getBody().get("message"));
    }
}
