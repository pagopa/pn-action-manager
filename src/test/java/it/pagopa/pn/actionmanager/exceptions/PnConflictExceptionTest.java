package it.pagopa.pn.actionmanager.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PnConflictExceptionTest {

    @Test
    void constructorShouldSetAllFieldsCorrectly() {
        String message = "Conflict occurred";
        String description = "Detailed conflict description";
        String errorCode = "ERR_CONFLICT";

        PnConflictException exception = new PnConflictException(message, description, errorCode);

        assertEquals(message, exception.getMessage());
    }
}