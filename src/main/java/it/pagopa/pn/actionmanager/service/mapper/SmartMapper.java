package it.pagopa.pn.actionmanager.service.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@Data
@AllArgsConstructor
public class SmartMapper {
    private final ObjectMapper objectMapper;
    private static ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STANDARD)
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public static <S,T> T mapToClass(S source, Class<T> destinationClass ){
        T result;
        if (source != null) {
            result = modelMapper.map(source, destinationClass);
        } else {
            result = null;
        }
        return result;
    }

    public Map<String, Object> mapFromJsonStringToMap(String source) {
        try {
            return objectMapper.readValue(source, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            log.error("Error in JSON = {} deserialization", source);
            throw new IllegalArgumentException("Error in JSON deserialization", e);
        }
    }
}
