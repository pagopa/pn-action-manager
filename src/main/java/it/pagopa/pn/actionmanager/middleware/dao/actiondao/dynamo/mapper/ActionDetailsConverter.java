package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.*;

public class ActionDetailsConverter implements AttributeConverter<Map<String, Object>> {

    @Override
    public AttributeValue transformFrom(Map<String, Object> details) {
        if (Objects.isNull(details)) {
            return AttributeValue.builder().m(new HashMap<>()).build();
        }
        Map<String, AttributeValue> attributeMap = mapToAttributeValueMap(details);
        return AttributeValue.builder().m(attributeMap).build();
    }

    @Override
    public Map<String, Object> transformTo(AttributeValue attributeValue) {
        return new HashMap<>();
    }

    @Override
    public EnhancedType<Map<String, Object>> type() {
        return null;
    }

    @Override
    public AttributeValueType attributeValueType() {
        return null;
    }

    /**
     * Converte una mappa Java in una mappa di AttributeValue di DynamoDB,
     * preservando le tipizzazioni corrette per ogni elemento
     */
    private Map<String, AttributeValue> mapToAttributeValueMap(Map<String, Object> map) {
        Map<String, AttributeValue> result = new HashMap<>();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            result.put(key, convertToAttributeValue(value));
        }

        return result;
    }

    /**
     * Converte un oggetto Java nel corrispondente AttributeValue di DynamoDB
     */
    private AttributeValue convertToAttributeValue(Object value) {
        switch (value) {
            case null -> {
                return AttributeValue.builder().nul(true).build();
            }
            case String s -> {
                return AttributeValue.builder().s(s).build();
            }
            case Number ignored -> {
                return AttributeValue.builder().n(value.toString()).build();
            }
            case Boolean b -> {
                return AttributeValue.builder().bool(b).build();
            }
            case List list -> {
                List<AttributeValue> listItems = new ArrayList<>();
                for (Object item : list) {
                    listItems.add(convertToAttributeValue(item));
                }
                return AttributeValue.builder().l(listItems).build();
            }
            case Map map -> {
                return AttributeValue.builder()
                        .m(mapToAttributeValueMap((Map<String, Object>) value))
                        .build();
            }
            default -> {
                return AttributeValue.builder().s(value.toString()).build();
            }
        }
    }
}