package it.pagopa.pn.actionmanager.it.utils;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DynamoDbConverterEvaluatorTest {

    private static final Map<String, AttributeValue> ITEM_MAP = Map.of(
            "id", AttributeValue.builder().s("123").build(),
            "count", AttributeValue.builder().n("42").build(),
            "active", AttributeValue.builder().bool(true).build(),
            "tags", AttributeValue.builder().ss("tag1", "tag2", "tag3").build(),
            "emptyList", AttributeValue.builder().l(new ArrayList<>()).build(),
            "emptySet", AttributeValue.builder().ns().build(),
            "emptyMap", AttributeValue.builder().m(Map.of()).build(),
            "nullValue", AttributeValue.builder().nul(true).build()
    );

    @Test
    void testVerifyConversion_S_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "id", Type.S, "123");
    }

    @Test
    void testVerifyConversion_N_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "count", Type.N, "42");
    }

    @Test
    void testVerifyConversion_BOOL_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "active", Type.BOOL, true);
    }

    @Test
    void testVerifyConversion_BOOL_KO_TYPE() {
        AssertionError error = assertThrows(AssertionError.class, () ->
                DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "id", Type.BOOL)
        );
        assertTrue(error.getMessage().contains("L'attributo 'id' non è del tipo atteso"));
    }

    @Test
    void testVerifyConversion_BOOL_KO_VALUE() {
        AssertionError error = assertThrows(AssertionError.class, () ->
            DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "active", Type.BOOL, false)
        );
        assertTrue(error.getMessage().contains("L'attributo 'active' non ha il valore atteso"));
    }

    @Test
    void testVerifyConversion_SS_WHOLE_MATCH_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "tags", Type.SS, List.of("tag1", "tag2", "tag3"));
    }

    @Test
    void testVerifyConversion_SS_CONTAINS_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "tags", Type.SS, "tag1");
    }

    @Test
    void testVerifyConversion_SS_PARTIAL_MATCH_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "tags", Type.SS, List.of("tag1", "tag2"));
    }

    @Test
    void testVerifyConversion_NULL_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "nullValue", Type.NUL);
    }

    @Test
    void testVerifyConversion_NULL_KO() {
        AssertionError error = assertThrows(AssertionError.class, () ->
            DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "id", Type.NUL)
        );
        assertTrue(error.getMessage().contains("L'attributo 'id' non è del tipo atteso"));
    }

    @Test
    void testVerifyConversion_L_OK() {
        DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "emptyList", Type.L, List.of());
    }

    @Test
    void testVerifyConversion_L_KO() {
        List<String> list = List.of();
        AssertionError error = assertThrows(AssertionError.class, () ->
            DynamoDbConverterEvaluator.verifyConversion(ITEM_MAP, "active", Type.L, list)
        );
        assertTrue(error.getMessage().contains("L'attributo 'active' non è del tipo atteso"));
    }
}