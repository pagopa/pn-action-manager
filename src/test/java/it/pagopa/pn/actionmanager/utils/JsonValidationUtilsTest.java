package it.pagopa.pn.actionmanager.utils;

import it.pagopa.pn.actionmanager.exceptions.PnBadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JsonValidationUtilsTest {

    @Test
    void testValidateJsonString_blankOrNull() {
        // Non deve lanciare eccezioni
        assertDoesNotThrow(() -> JsonValidationUtils.validateJsonString(null, 100, 5));
        assertDoesNotThrow(() -> JsonValidationUtils.validateJsonString("", 100, 5));
        assertDoesNotThrow(() -> JsonValidationUtils.validateJsonString("   ", 100, 5));
    }

    @Test
    void testValidateJsonString_exceedsMaxSize() {
        String json = "{\"a\":\"" + "x".repeat(200) + "\"}";
        int maxSize = 10;
        assertThrows(PnBadRequestException.class, () -> JsonValidationUtils.validateJsonString(json, maxSize, 5));
    }

    @Test
    void testValidateJsonString_exceedsMaxDepth() {
        String json = "{\"a\":{\"b\":{\"c\":{\"d\":1}}}}"; // profondità 4
        assertThrows(PnBadRequestException.class, () -> JsonValidationUtils.validateJsonString(json, 10_000, 2));
    }

    @Test
    void testValidateJsonString_invalidJson() {
        String invalidJson = "{not valid json}";
        assertThrows(IllegalArgumentException.class, () -> JsonValidationUtils.validateJsonString(invalidJson, 100, 5));
    }

    @Test
    void testValidateJsonString_validJson() {
        String json = "{\"a\":1,\"b\":[2,3]}";
        assertDoesNotThrow(() -> JsonValidationUtils.validateJsonString(json, 100, 5));
    }
}