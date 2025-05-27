package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class DtoToEntityFutureActionMapperTest {

    @Test
    void dtoToEntity() {
        Action action = buildAction();
        FutureActionEntity expected = buildFutureActionEntity();
        FutureActionEntity actual = DtoToEntityFutureActionMapper.dtoToEntity(action, "2021-09-16T15:24:00.00Z");
        Assertions.assertEquals(expected, actual);
    }

    private Action buildAction() {
        Instant instant = Instant.parse("2021-09-16T15:24:00.00Z");

        return Action.builder()
                .iun("001")
                .actionId("002")
                .timeslot("2021-09-16T15:24:00.00Z")
                .notBefore(instant)
                .recipientIndex(1)
                .type(ActionType.ANALOG_WORKFLOW)
                .timelineId("2021-09-16T15:24:00.00Z")
                .build();
    }

    private FutureActionEntity buildFutureActionEntity() {
        Instant instant = Instant.parse("2021-09-16T15:24:00.00Z");

        return FutureActionEntity.builder()
                .iun("001")
                .actionId("002")
                .notBefore(instant)
                .recipientIndex(1)
                .type(ActionType.ANALOG_WORKFLOW)
                .timelineId("2021-09-16T15:24:00.00Z")
                .timeSlot("2021-09-16T15:24:00.00Z")
                .details(null)
                .build();
    }
}