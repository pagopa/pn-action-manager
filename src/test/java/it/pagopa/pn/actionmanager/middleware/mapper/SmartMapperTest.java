package it.pagopa.pn.actionmanager.middleware.mapper;

import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SmartMapperTest {

    @Test
    void mapFromStringToMap_validJson_returnsMap() {
        String json = "{\"key\":\"value\", \"num\":123, \"bool\":true}";
        Map<String, Object> result = SmartMapper.mapFromStringToMap(json);
        assertEquals("value", result.get("key"));
        assertEquals(123, ((Number) result.get("num")).intValue());
        assertEquals(true, result.get("bool"));
    }

    @Test
    void mapFromStringToMap_invalidJson_throwsIllegalArgumentException() {
        String invalidJson = "{invalid json}";
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                SmartMapper.mapFromStringToMap(invalidJson)
        );
        assertTrue(ex.getMessage().contains("Error in JSON deserialization"));
    }

    @Test
    void mapFromStringToMap_emptyJson_returnsEmptyMap() {
        String emptyJson = "{}";
        Map<String, Object> result = SmartMapper.mapFromStringToMap(emptyJson);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}