package it.pagopa.pn.actionmanager.middleware.mapper;

import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.ActionDetailsConverter;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ActionDetailsConverterTest {

    private final ActionDetailsConverter converter = new ActionDetailsConverter();

    @Test
    void testTransformFrom_withNullMap() {
        AttributeValue result = converter.transformFrom(null);
        assertNotNull(result);
        assertTrue(result.hasM());
        assertTrue(result.m().isEmpty());
    }

    @Test
    void testTransformFrom_withSimpleTypes() {
        Map<String, Object> input = new HashMap<>();
        input.put("string", "value");
        input.put("number", 123);
        input.put("boolean", true);
        input.put("null", null);

        AttributeValue result = converter.transformFrom(input);
        Map<String, AttributeValue> map = result.m();

        assertEquals("value", map.get("string").s());
        assertEquals("123", map.get("number").n());
        assertTrue(map.get("boolean").bool());
        assertTrue(map.get("null").nul());
    }

    @Test
    void testTransformFrom_withListAndMap() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("nestedKey", "nestedValue");

        List<Object> list = Arrays.asList("a", 1, false);

        Map<String, Object> input = new HashMap<>();
        input.put("list", list);
        input.put("map", nested);

        AttributeValue result = converter.transformFrom(input);

        // Verifica lista
        List<AttributeValue> listValues = result.m().get("list").l();
        assertEquals("a", listValues.get(0).s());
        assertEquals("1", listValues.get(1).n());
        assertFalse(listValues.get(2).bool());

        // Verifica mappa annidata
        Map<String, AttributeValue> nestedMap = result.m().get("map").m();
        assertEquals("nestedValue", nestedMap.get("nestedKey").s());
    }
}