package it.pagopa.pn.actionmanager.utils;


import it.pagopa.pn.actionmanager.exceptions.PnBadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DetailsValidationUtilsTest {

    @Test
    void testValidateDetails_blankOrNull() {
        // Non deve lanciare eccezioni
        assertDoesNotThrow(() -> DetailsValidationUtils.validateDetails(null, 100, 5));
        assertDoesNotThrow(() -> DetailsValidationUtils.validateDetails("", 100, 5));
        assertDoesNotThrow(() -> DetailsValidationUtils.validateDetails("   ", 100, 5));
    }

    @Test
    void testValidateDetails_exceedsMaxSize() {
        String json = "{\"a\":\"" + "x".repeat(200) + "\"}";
        int maxSize = 10;
        assertThrows(PnBadRequestException.class, () -> DetailsValidationUtils.validateDetails(json, maxSize, 5));
    }

    @Test
    void testValidateDetails_exceedsMaxDepth() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":1}}}}"; // profondità 4
        assertThrows(PnBadRequestException.class, () -> DetailsValidationUtils.validateDetails(json, 10_000, 2));
    }

    @Test
    void testValidateJsonString_invalidJson() {
        String invalidJson = "{not valid json}";
        assertThrows(PnBadRequestException.class, () -> DetailsValidationUtils.validateDetails(invalidJson, 100, 5));
    }

    @Test
    void testValidateJsonString_validJson() {
        String json = "{\"a\":1,\"b\":[2,3]}";
        assertDoesNotThrow(() -> DetailsValidationUtils.validateDetails(json, 100, 5));
    }
}