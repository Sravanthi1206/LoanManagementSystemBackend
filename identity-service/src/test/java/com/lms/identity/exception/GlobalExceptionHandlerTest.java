package com.lms.identity.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    public void testHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleUserNotFoundException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found", response.getBody().get("message"));
    }

    @Test
    public void testHandleDuplicateUserException() {
        DuplicateUserException ex = new DuplicateUserException("User exists");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleDuplicateUserException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User exists", response.getBody().get("message"));
    }

    @Test
    public void testHandleInvalidCredentialsException() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid creds");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidCredentialsException(ex, webRequest);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid creds", response.getBody().get("message"));
    }

    @Test
    public void testHandleInvalidRoleException() {
        InvalidRoleException ex = new InvalidRoleException("Invalid role");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleInvalidRoleException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid role", response.getBody().get("message"));
    }

    @Test
    public void testHandleGlobalException() {
        Exception ex = new Exception("Internal error");
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal error", response.getBody().get("message"));
    }

    @Test
    public void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "error message");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(webRequest.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, String> errors = (Map<String, String>) response.getBody().get("errors");
        assertEquals("error message", errors.get("field"));
    }
}
