package it.pagopa.pn.actionmanager.it;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.ActionDetailsConverter;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue.Type;

import java.util.List;
import java.util.Map;

import static it.pagopa.pn.actionmanager.it.utils.DynamoDbConverterEvaluator.verifyConversion;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"unchecked"})
public class DetailsMapperIT {
    private final ActionDetailsConverter actionDetailsConverter = new ActionDetailsConverter();
    private final SmartMapper smartMapper = new SmartMapper(new ObjectMapper());

    private static final String DETAILS_START_RECIPIENT_WORKFLOW = """
            {
              "quickAccessLinkToken": "TlVZWi1OSkVXLURZVUotMjAyNTAzLVUtMV9QRi1iM2NjYWMzMS0zOGVhLTQ0Y2QtOTYwMS05ZjJkMTllODUzYWZfNDU0MGQ5MDMtNjczYy00YjM0LTlhMDEtN2QwNGNhMzU5OGVj",
              "retryAttempt": 0
             }""";

    private static final String DETAILS_DOCUMENT_CREATION_RESPONSE = """
            {
              "documentCreationType": "DIGITAL_DELIVERY",
              "key": "safestorage://PN_LEGAL_FACTS-8e9217ab793443c8aae0e5b57c2338e4.pdf",
              "retryAttempt": 0,
              "timelineId": "DIGITAL_DELIVERY_CREATION_REQUEST.IUN_KXQM-DYGY-LNJA-202410-H-1.RECINDEX_0"
             }""";

    private static final String DETAILS_NOTIFICATION_VALIDATION = """
            {
              "retryAttempt": 0,
              "startWorkflowTime": "2025-05-27T09:53:42.703930640Z"
             }""";

    private static final String DETAILS_NOTIFICATION_REFUSED = """
             {
              "errors": [
               {
                "detail": "Address not found for recipient index: 0",
                "errorCode": "ADDRESS_NOT_FOUND",
                "recIndex": 0
               },
               {
                "detail": "Address not found for recipient index: 2",
                "errorCode": "ADDRESS_NOT_FOUND",
                "recIndex": 2
               }
              ],
              "retryAttempt": 0
             }\
            """;

    private static final String DETAILS_SEND_DIGITAL_FINAL_STATUS_RESPONSE = """
            {
              "isFirstSendRetry": false,
              "lastAttemptAddressInfo": {
               "lastAttemptDate": "2025-03-05T19:40:29.656258472Z",
               "relatedFeedbackTimelineId": "SEND_DIGITAL_FEEDBACK.IUN_VKJH-TPZN-YUPU-202503-D-1.RECINDEX_0.SOURCE_SPECIAL.REPEAT_false.ATTEMPT_0",
               "sentAttemptMade": 0
              },
              "retryAttempt": 0
             }""";

    @Test
    public void testMappers_DetailsStartRecipientWorkflow() {
        // Converto un JSON rappresentante un detail associato ad azioni con tipologia START_RECIPIENT_WORKFLOW in una mappa
        Map<String, Object> detailsInt = smartMapper.mapFromJsonStringToMap(DETAILS_START_RECIPIENT_WORKFLOW);
        assertNotNull(detailsInt);
        assertEquals(2, detailsInt.size());
        assertTrue(detailsInt.containsKey("quickAccessLinkToken"));
        assertEquals("TlVZWi1OSkVXLURZVUotMjAyNTAzLVUtMV9QRi1iM2NjYWMzMS0zOGVhLTQ0Y2QtOTYwMS05ZjJkMTllODUzYWZfNDU0MGQ5MDMtNjczYy00YjM0LTlhMDEtN2QwNGNhMzU5OGVj", detailsInt.get("quickAccessLinkToken"));
        assertTrue(detailsInt.containsKey("retryAttempt"));
        assertEquals(0, ((Number) detailsInt.get("retryAttempt")).intValue());

        // Converto la mappa in un AttributeValue, che rappresenta l'oggetto DynamoDB cercando di preservare le tipizzazioni corrette
        Map<String, AttributeValue> detailsEntity = actionDetailsConverter.transformFrom(detailsInt).m();

        // Verifico che la conversione sia avvenuta correttamente, con i tipi e i valori attesi
        verifyConversion(detailsEntity, "quickAccessLinkToken", Type.S, "TlVZWi1OSkVXLURZVUotMjAyNTAzLVUtMV9QRi1iM2NjYWMzMS0zOGVhLTQ0Y2QtOTYwMS05ZjJkMTllODUzYWZfNDU0MGQ5MDMtNjczYy00YjM0LTlhMDEtN2QwNGNhMzU5OGVj");
        verifyConversion(detailsEntity, "retryAttempt", Type.N, 0);
    }

    @Test
    public void testMappers_DetailsDocumentCreationResponse() {
        // Converto un JSON rappresentante un detail associato ad azioni con tipologia DOCUMENT_CREATION_RESPONSE in una mappa
        Map<String, Object> detailsInt = smartMapper.mapFromJsonStringToMap(DETAILS_DOCUMENT_CREATION_RESPONSE);

        assertEquals("DIGITAL_DELIVERY", detailsInt.get("documentCreationType"));
        assertEquals("safestorage://PN_LEGAL_FACTS-8e9217ab793443c8aae0e5b57c2338e4.pdf", detailsInt.get("key"));
        assertEquals(0, ((Number) detailsInt.get("retryAttempt")).intValue());
        assertEquals("DIGITAL_DELIVERY_CREATION_REQUEST.IUN_KXQM-DYGY-LNJA-202410-H-1.RECINDEX_0", detailsInt.get("timelineId"));
        assertEquals(4, detailsInt.size());

        // Converto la mappa in un AttributeValue, che rappresenta l'oggetto DynamoDB cercando di preservare le tipizzazioni corrette
        Map<String, AttributeValue> detailsEntity = actionDetailsConverter.transformFrom(detailsInt).m();

        //Verifico che la conversione sia avvenuta correttamente, con i tipi e i valori attesi
        verifyConversion(detailsEntity, "documentCreationType", Type.S, "DIGITAL_DELIVERY");
        verifyConversion(detailsEntity, "key", Type.S, "safestorage://PN_LEGAL_FACTS-8e9217ab793443c8aae0e5b57c2338e4.pdf");
        verifyConversion(detailsEntity, "retryAttempt", Type.N, 0);
        verifyConversion(detailsEntity, "timelineId", Type.S, "DIGITAL_DELIVERY_CREATION_REQUEST.IUN_KXQM-DYGY-LNJA-202410-H-1.RECINDEX_0");
    }

    @Test
    public void testMappers_DetailsNotificationValidation() {
        // Converto un JSON rappresentante un detail associato ad azioni con tipologia NOTIFICATION_VALIDATION in una mappa
        Map<String, Object> detailsInt = smartMapper.mapFromJsonStringToMap(DETAILS_NOTIFICATION_VALIDATION);
        assertNotNull(detailsInt);
        assertEquals(2, detailsInt.size());
        assertTrue(detailsInt.containsKey("retryAttempt"));
        assertEquals(0, ((Number) detailsInt.get("retryAttempt")).intValue());
        assertTrue(detailsInt.containsKey("startWorkflowTime"));
        assertEquals("2025-05-27T09:53:42.703930640Z", detailsInt.get("startWorkflowTime"));

        // Converto la mappa in un AttributeValue, che rappresenta l'oggetto DynamoDB cercando di preservare le tipizzazioni corrette
        Map<String, AttributeValue> detailsEntity = actionDetailsConverter.transformFrom(detailsInt).m();

        // Verifico che la conversione sia avvenuta correttamente, con i tipi e i valori attesi
        verifyConversion(detailsEntity, "retryAttempt", Type.N, 0);
        verifyConversion(detailsEntity, "startWorkflowTime", Type.S, "2025-05-27T09:53:42.703930640Z");
    }

    @Test
    public void testMappers_DetailsNotificationRefused() {
        // Converto un JSON rappresentante un detail associato ad azioni con tipologia NOTIFICATION_REFUSED in una mappa
        Map<String, Object> detailsInt = smartMapper.mapFromJsonStringToMap(DETAILS_NOTIFICATION_REFUSED);
        assertNotNull(detailsInt);
        assertEquals(2, detailsInt.size());
        assertTrue(detailsInt.containsKey("retryAttempt"));
        assertEquals(0, ((Number) detailsInt.get("retryAttempt")).intValue());
        assertTrue(detailsInt.containsKey("errors"));
        assertInstanceOf(List.class, detailsInt.get("errors"));
        assertEquals(2, ((List<?>) detailsInt.get("errors")).size());
        assertEquals("ADDRESS_NOT_FOUND", ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).getFirst()).get("errorCode"));
        assertEquals("Address not found for recipient index: 0", ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).get(0)).get("detail"));
        assertEquals(0, ((Number) ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).get(0)).get("recIndex")).intValue());
        assertEquals("ADDRESS_NOT_FOUND", ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).get(1)).get("errorCode"));
        assertEquals("Address not found for recipient index: 2", ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).get(1)).get("detail"));
        assertEquals(2, ((Number) ((Map<String, Object>) ((List<?>) detailsInt.get("errors")).get(1)).get("recIndex")).intValue());

        // Converto la mappa in un AttributeValue, che rappresenta l'oggetto DynamoDB cercando di preservare le tipizzazioni corrette
        Map<String, AttributeValue> detailsEntity = actionDetailsConverter.transformFrom(detailsInt).m();

        // Verifico che la conversione sia avvenuta correttamente, con i tipi e i valori attesi
        verifyConversion(detailsEntity, "retryAttempt", Type.N, 0);
        verifyConversion(detailsEntity, "errors", Type.L);
        Map<String, AttributeValue> firstError = detailsEntity.get("errors").l().getFirst().m();
        verifyConversion(firstError, "errorCode", Type.S, "ADDRESS_NOT_FOUND");
        verifyConversion(firstError, "detail", Type.S, "Address not found for recipient index: 0");
        verifyConversion(firstError, "recIndex", Type.N, 0);
        Map<String, AttributeValue> secondError = detailsEntity.get("errors").l().get(1).m();
        verifyConversion(secondError, "errorCode", Type.S, "ADDRESS_NOT_FOUND");
        verifyConversion(secondError, "detail", Type.S, "Address not found for recipient index: 2");
        verifyConversion(secondError, "recIndex", Type.N, 2);
    }

    @Test
    public void testMappers_DetailsSendDigitalFinalStatusResponse() {
        // Converto un JSON rappresentante un detail associato ad azioni con tipologia SEND_DIGITAL_FINAL_STATUS_RESPONSE in una mappa
        Map<String, Object> detailsInt = smartMapper.mapFromJsonStringToMap(DETAILS_SEND_DIGITAL_FINAL_STATUS_RESPONSE);
        assertNotNull(detailsInt);
        assertEquals(3, detailsInt.size());
        assertTrue(detailsInt.containsKey("isFirstSendRetry"));
        assertFalse((Boolean) detailsInt.get("isFirstSendRetry"));
        assertTrue(detailsInt.containsKey("lastAttemptAddressInfo"));
        assertInstanceOf(Map.class, detailsInt.get("lastAttemptAddressInfo"));
        Map<String, Object> lastAttemptAddressInfoInt = (Map<String, Object>) detailsInt.get("lastAttemptAddressInfo");
        assertEquals("2025-03-05T19:40:29.656258472Z", lastAttemptAddressInfoInt.get("lastAttemptDate"));
        assertEquals("SEND_DIGITAL_FEEDBACK.IUN_VKJH-TPZN-YUPU-202503-D-1.RECINDEX_0.SOURCE_SPECIAL.REPEAT_false.ATTEMPT_0", lastAttemptAddressInfoInt.get("relatedFeedbackTimelineId"));
        assertEquals(0, ((Number) lastAttemptAddressInfoInt.get("sentAttemptMade")).intValue());
        assertTrue(detailsInt.containsKey("retryAttempt"));
        assertEquals(0, ((Number) detailsInt.get("retryAttempt")).intValue());

        // Converto la mappa in un AttributeValue, che rappresenta l'oggetto DynamoDB cercando di preservare le tipizzazioni corrette
        Map<String, AttributeValue> detailsEntity = actionDetailsConverter.transformFrom(detailsInt).m();

        // Verifico che la conversione sia avvenuta correttamente
        verifyConversion(detailsEntity, "isFirstSendRetry", Type.BOOL, false);
        verifyConversion(detailsEntity, "lastAttemptAddressInfo", Type.M);
        Map<String, AttributeValue> lastAttemptAddressInfoEnt = detailsEntity.get("lastAttemptAddressInfo").m();
        verifyConversion(lastAttemptAddressInfoEnt, "lastAttemptDate", Type.S, "2025-03-05T19:40:29.656258472Z");
        verifyConversion(lastAttemptAddressInfoEnt, "relatedFeedbackTimelineId", Type.S, "SEND_DIGITAL_FEEDBACK.IUN_VKJH-TPZN-YUPU-202503-D-1.RECINDEX_0.SOURCE_SPECIAL.REPEAT_false.ATTEMPT_0");
        verifyConversion(lastAttemptAddressInfoEnt, "sentAttemptMade", Type.N, 0);
        verifyConversion(detailsEntity, "retryAttempt", Type.N, 0);
    }
}

