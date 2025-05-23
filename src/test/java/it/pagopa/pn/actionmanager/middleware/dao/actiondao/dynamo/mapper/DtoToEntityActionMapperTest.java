package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.dto.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

@SpringBootTest
class DtoToEntityActionMapperTest {

    @Test
    void dtoToEntity_shouldMapCorrectly() {
        // Arrange
        SmartMapper smartMapper = Mockito.mock(SmartMapper.class);
        Mockito.when(smartMapper.mapFromStringToMap(anyString()))
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

        ActionEntity expected = ActionEntity.builder()
                .iun("001")
                .actionId("002")
                .notBefore(instant)
                .recipientIndex(1)
                .details(Map.of("key", "value"))
                .type(ActionType.ANALOG_WORKFLOW)
                .timelineId("2021-09-16T15:24:00.00Z")
                .build();

        // Act
        ActionEntity actual = mapper.dtoToEntity(action, Duration.ZERO);

        // Assert
        assertEquals(expected, actual);
    }
}