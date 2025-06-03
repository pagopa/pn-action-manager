package it.pagopa.pn.actionmanager.it.utils;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.Type;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({"unchecked"})
public class DynamoDbConverterEvaluator {
    private DynamoDbConverterEvaluator() {
        // Private constructor to prevent instantiation
    }
    private static void validateInputs(Map<String, AttributeValue> attributeMap, String key, Type expectedType) {
        assertNotNull(attributeMap, "La mappa degli attributi non può essere null");
        assertNotNull(key, "La chiave non può essere null");
        assertFalse(key.trim().isEmpty(), "La chiave non può essere vuota");
        assertNotNull(expectedType, "Il tipo atteso non può essere null");

        AttributeValue attributeValue = attributeMap.get(key);
        assertNotNull(attributeValue, String.format("Attributo con chiave '%s' non trovato nella mappa", key));
    }

    /**
     * Verifica che l'attributo specificato nella mappa sia del tipo DynamoDB atteso
     *
     * @param attributeMap La mappa contenente gli AttributeValue
     * @param key La chiave dell'attributo da verificare
     * @param expectedType Il tipo DynamoDB atteso (enum)
     */
    public static void verifyConversion(Map<String, AttributeValue> attributeMap, String key, Type expectedType) {
        validateInputs(attributeMap, key, expectedType);

        AttributeValue attributeValue = attributeMap.get(key);
        assertEquals(attributeValue.type(), expectedType,
                String.format("L'attributo '%s' non è del tipo atteso '%s'", key, expectedType.name()));
    }

    /**
     * Verifica che l'attributo specificato nella mappa sia del tipo DynamoDB atteso
     * e che abbia un valore specifico
     *
     * @param attributeMap La mappa contenente gli AttributeValue
     * @param key La chiave dell'attributo da verificare
     * @param expectedType Il tipo DynamoDB atteso (enum)
     * @param expectedValue Il valore atteso (opzionale, può essere null)
     */
    public static void verifyConversion(Map<String, AttributeValue> attributeMap, String key, Type expectedType, Object expectedValue) {
        verifyConversion(attributeMap, key, expectedType);

        AttributeValue attributeValue = attributeMap.get(key);
        Object actualValue = extractActualValue(attributeValue, expectedType);
        validateExpectedValue(key, expectedType, expectedValue, actualValue);
    }

    private static Object extractActualValue(AttributeValue attributeValue, Type expectedType) {
        return switch (expectedType) {
            case S -> attributeValue.s();
            case N -> attributeValue.n();
            case B -> attributeValue.b();
            case SS -> attributeValue.ss();
            case NS -> attributeValue.ns();
            case BS -> attributeValue.bs();
            case M -> attributeValue.m();
            case L -> attributeValue.l();
            case BOOL -> attributeValue.bool();
            case NUL -> attributeValue.nul();
            case UNKNOWN_TO_SDK_VERSION -> null;
        };
    }

    private static void validateExpectedValue(String key, Type expectedType, Object expectedValue, Object actualValue) {
        switch (expectedType) {
            case S, BOOL -> assertEquals(expectedValue, actualValue,
                    String.format("L'attributo '%s' non ha il valore atteso", key));
            case N -> {
                String expectedStr = expectedValue instanceof String ? (String) expectedValue : expectedValue.toString();
                assertEquals(expectedStr, actualValue,
                        String.format("L'attributo '%s' non ha il valore numerico atteso", key));
            }
            case SS, NS, BS -> {
                if (expectedValue instanceof Collection) {
                    assertTrue(((Collection<?>) actualValue).containsAll((Collection<?>) expectedValue),
                            String.format("L'attributo '%s' non contiene tutti i valori attesi nel set", key));
                } else {
                    assertTrue(((Collection<?>) actualValue).contains(expectedValue),
                            String.format("L'attributo '%s' non contiene il valore atteso nel set", key));
                }
            }
            case M -> {
                if (expectedValue instanceof Map) {
                    Map<String, AttributeValue> actualMap = (Map<String, AttributeValue>) actualValue;
                    Map<String, ?> expectedMap = (Map<String, ?>) expectedValue;
                    for (Map.Entry<String, ?> entry : expectedMap.entrySet()) {
                        assertTrue(actualMap.containsKey(entry.getKey()),
                                String.format("La mappa '%s' non contiene la chiave attesa '%s'", key, entry.getKey()));
                    }
                }
            }
            case L -> {
                if (expectedValue instanceof Collection) {
                    List<AttributeValue> actualList = (List<AttributeValue>) actualValue;
                    assertEquals(((Collection<?>) expectedValue).size(), actualList.size(),
                            String.format("La lista '%s' non ha la dimensione attesa", key));
                }
            }
            case NUL -> assertEquals(Boolean.TRUE, actualValue,
                    String.format("L'attributo '%s' dovrebbe essere null", key));
        }
    }

}
