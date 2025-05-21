package it.pagopa.pn.actionmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;


@Slf4j
@Component
@Data
@NoArgsConstructor
public class SmartMapper {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> mapFromStringToMap(String source) {
        try {
            return objectMapper.readValue(source, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Error in JSON = {} deserialization", source);
            throw new IllegalArgumentException("Error in JSON deserialization", e);
        }
    }
}
