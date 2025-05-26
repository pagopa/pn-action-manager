package it.pagopa.pn.actionmanager.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.actionmanager.exceptions.PnBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.utils.StringUtils;

import java.nio.charset.StandardCharsets;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTION_BAD_REQUEST;
import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_MESSAGE_INVALID_DETAILS_FIELD;

@Slf4j
@Component
public class DetailsValidationUtils {
    private DetailsValidationUtils() { }

    /**
     * Validates a JSON string by checking its size and depth.
     */
    public static void validateDetails(String details, int maxSizeBytes, int maxDepth) {
        if (StringUtils.isBlank(details)) {
            log.info("Field details is blank or null");
            return;
        }
        if (details.getBytes(StandardCharsets.UTF_8).length > maxSizeBytes) {
            log.warn("details exceeds maximum allowed size: current size = {} bytes, max allowed = {} bytes",
                    details.getBytes(StandardCharsets.UTF_8).length, maxSizeBytes);
            throw new PnBadRequestException(ERROR_MESSAGE_INVALID_DETAILS_FIELD, "Field details exceeds maximum allowed size", ERROR_CODE_ACTION_BAD_REQUEST);
        }
        try {
            JsonNode node = new ObjectMapper().readTree(details);
            int depth = getJsonDepth(node, 0);
            if (depth > maxDepth) {
                log.error("JSON string exceeds maximum allowed depth: current depth = {}, max allowed = {}", depth, maxDepth);
                throw new PnBadRequestException(ERROR_MESSAGE_INVALID_DETAILS_FIELD, "Field details exceeds maximum allowed depth", ERROR_CODE_ACTION_BAD_REQUEST);
            }
        } catch (JsonProcessingException e) {
            log.error("Invalid JSON string: parsing failed", e);
            throw new PnBadRequestException(ERROR_MESSAGE_INVALID_DETAILS_FIELD, "Field details is not a valid JSON", ERROR_CODE_ACTION_BAD_REQUEST);
        }
    }

    /**
     * Calcola la profondità di un nodo JSON.
     */
    private static int getJsonDepth(JsonNode node, int depth) {
        if (node.isObject() || node.isArray()) {
            int max = depth;
            for (JsonNode child : node) {
                int childDepth = getJsonDepth(child, depth + 1);
                if (childDepth > max) {
                    max = childDepth;
                }
            }
            return max;
        }
        return depth;
    }
}
