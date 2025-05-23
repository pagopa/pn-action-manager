package it.pagopa.pn.actionmanager.middleware.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SmartMapperTest {
    @Mock
    private ObjectMapper objectMapper;

    private SmartMapper smartMapper = new SmartMapper(new ObjectMapper());

    @Test
    void mapFromStringToMap_validJson_returnsMap() {
        String json = "{\"key\":\"value\", \"num\":123, \"bool\":true}";
        Map<String, Object> result = smartMapper.mapFromStringToMap(json);
        assertEquals("value", result.get("key"));
        assertEquals(123, ((Number) result.get("num")).intValue());
        assertEquals(true, result.get("bool"));
    }

    @Test
    void mapFromStringToMap_invalidJson_throwsIllegalArgumentException() {
        String invalidJson = "{invalid json}";
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                smartMapper.mapFromStringToMap(invalidJson)
        );
        assertTrue(ex.getMessage().contains("Error in JSON deserialization"));
    }

    @Test
    void mapFromStringToMap_emptyJson_returnsEmptyMap() {
        String emptyJson = "{}";
        Map<String, Object> result = smartMapper.mapFromStringToMap(emptyJson);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}