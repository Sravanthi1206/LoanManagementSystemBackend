package com.lms.identity.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentityExceptionTest {

    @Test
    public void testExceptions() {
        UserNotFoundException ex1 = new UserNotFoundException("User not found");
        assertEquals("User not found", ex1.getMessage());

        DuplicateUserException ex2 = new DuplicateUserException("Duplicate user");
        assertEquals("Duplicate user", ex2.getMessage());

        InvalidCredentialsException ex3 = new InvalidCredentialsException("Invalid creds");
        assertEquals("Invalid creds", ex3.getMessage());

        InvalidRoleException ex4 = new InvalidRoleException("Invalid role");
        assertEquals("Invalid role", ex4.getMessage());
    }
}
