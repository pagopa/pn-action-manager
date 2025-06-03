package it.pagopa.pn.actionmanager.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PnNotFoundExceptionTest {
    @Test
    void constructorShouldSetAllFieldsCorrectly() {
        String message = "Resource not found";
        String description = "Detailed not found description";
        String errorCode = "ERR_NOT_FOUND";

        PnNotFoundException exception = new PnNotFoundException(message, description, errorCode);

        assertEquals(message, exception.getMessage());
    }
}