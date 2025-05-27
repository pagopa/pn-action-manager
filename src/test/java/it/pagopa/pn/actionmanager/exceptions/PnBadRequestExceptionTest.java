package it.pagopa.pn.actionmanager.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PnBadRequestExceptionTest {
    @Test
    void constructorShouldSetAllFieldsCorrectly() {
        String message = "Bad request occurred";
        String description = "Detailed bad request description";
        String errorCode = "ERR_BAD_REQUEST";

        PnBadRequestException exception = new PnBadRequestException(message, description, errorCode);

        assertEquals(message, exception.getMessage());
    }
}