package it.pagopa.pn.actionmanager.middleware.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked"})
class SmartMapperTest {

    private final SmartMapper smartMapper = new SmartMapper(new ObjectMapper());

    @Test
    void mapFromStringToMap_validJson_returnsMapJson() {
        String json = "{\"key\":\"value\", \"num\":123, \"bool\":true}";
        Map<String, Object> result = smartMapper.mapFromJsonStringToMap(json);
        assertEquals("value", result.get("key"));
        assertEquals(123, ((Number) result.get("num")).intValue());
        assertEquals(true, result.get("bool"));
    }

    @Test
    void mapFromStringToMap_validJson_returnsMapJson3() {
        String json = "{\"errors\":[{\"errorCode\":\"errorCode\",\"detail\":\"detail\",\"recIndex\":0},{\"errorCode\":\"errorCode 2\",\"detail\":\"detail 2\",\"recIndex\":1}]}";
        Map<String, Object> result = smartMapper.mapFromJsonStringToMap(json);
        assertNotNull(result);
        assertEquals(java.util.ArrayList.class, result.get("errors").getClass());
        assertEquals(2, ((java.util.List<?>) result.get("errors")).size());
        Map<String, Object> firstError = (Map<String, Object>) ((java.util.List<?>) result.get("errors")).getFirst();
        assertEquals("errorCode", firstError.get("errorCode"));
        assertEquals("detail", firstError.get("detail"));
        assertEquals(0, ((Number) firstError.get("recIndex")).intValue());
        Map<String, Object> secondError = (Map<String, Object>) ((java.util.List<?>) result.get("errors")).get(1);
        assertEquals("errorCode 2", secondError.get("errorCode"));
        assertEquals("detail 2", secondError.get("detail"));
        assertEquals(1, ((Number) secondError.get("recIndex")).intValue());
    }

    @Test
    void mapFromJsonStringToMap_invalidJson_throwsIllegalArgumentException() {
        String invalidJson = "{invalid json}";
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                smartMapper.mapFromJsonStringToMap(invalidJson)
        );
        assertTrue(ex.getMessage().contains("Error in JSON deserialization"));
    }

    @Test
    void mapFromStringToMap_emptyJson_returnsEmptyMapJson() {
        String emptyJson = "{}";
        Map<String, Object> result = smartMapper.mapFromJsonStringToMap(emptyJson);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}