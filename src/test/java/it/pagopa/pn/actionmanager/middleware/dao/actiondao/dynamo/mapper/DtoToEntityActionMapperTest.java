package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;

class DtoToEntityActionMapperTest {

    @Test
    void dtoToEntity_shouldMapCorrectlyWithDetails() {
        // Arrange
        SmartMapper smartMapper = Mockito.mock(SmartMapper.class);
        Mockito.when(smartMapper.mapFromJsonStringToMap(anyString()))
                .thenReturn(Map.of("key", "value"));

        DtoToEntityActionMapper mapper = new DtoToEntityActionMapper(smartMapper);

        Instant instant = Instant.parse("2021-09-16T15:24:00.00Z");
        Action action = Action.builder()
                .iun("001")
                .actionId("002")
                .notBefore(instant)
                .recipientIndex(1)
                .type(ActionType.ANALOG_WORKFLOW)
                .details("{\"key\":\"value\"}")
                .timelineId("2021-09-16T15:24:00.00Z")
                .build();

        // Act
        ActionEntity actual = mapper.dtoToEntity(action, Duration.ZERO);

        // Assert
        assertEquals("001", actual.getIun());
        assertEquals("002", actual.getActionId());
        assertEquals(instant, actual.getNotBefore());
        assertEquals(1, actual.getRecipientIndex());
        assertEquals(ActionType.ANALOG_WORKFLOW, actual.getType());
        assertEquals("2021-09-16T15:24:00.00Z", actual.getTimelineId());
        assertEquals(Map.of("key", "value"), actual.getDetails());
    }

    @Test
    void dtoToEntity_shouldMapCorrectlyWithoutDetails() {
        // Arrange
        SmartMapper smartMapper = Mockito.mock(SmartMapper.class);

        DtoToEntityActionMapper mapper = new DtoToEntityActionMapper(smartMapper);

        Instant instant = Instant.parse("2021-09-16T15:24:00.00Z");
        Action action = Action.builder()
                .iun("001")
                .actionId("002")
                .notBefore(instant)
                .recipientIndex(1)
                .type(ActionType.ANALOG_WORKFLOW)
                .timelineId("2021-09-16T15:24:00.00Z")
                .details(null)
                .build();

        // Act
        ActionEntity actual = mapper.dtoToEntity(action, Duration.ofDays(3));

        // Assert
        assertEquals("001", actual.getIun());
        assertEquals("002", actual.getActionId());
        assertEquals(instant, actual.getNotBefore());
        assertEquals(1, actual.getRecipientIndex());
        assertEquals(ActionType.ANALOG_WORKFLOW, actual.getType());
        assertEquals("2021-09-16T15:24:00.00Z", actual.getTimelineId());
        assertNull(actual.getDetails());
    }
}