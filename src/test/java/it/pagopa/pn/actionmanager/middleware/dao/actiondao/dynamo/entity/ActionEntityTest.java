package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity;


import it.pagopa.pn.actionmanager.dto.action.ActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class ActionEntityTest {

    private ActionEntity entity;

    @BeforeEach
    void setup() {
        entity = new ActionEntity();
    }

    @Test
    void getActionId() {
        entity.setActionId("001");
        Assertions.assertEquals("001", entity.getActionId());
    }

    @Test
    void getNotBefore() {
        Instant in = Instant.parse("2021-09-16T15:24:00.00Z");
        entity.setNotBefore(in);
        Assertions.assertEquals(in, entity.getNotBefore());
    }

    @Test
    void getType() {
        entity.setType(ActionType.ANALOG_WORKFLOW);
        Assertions.assertEquals(ActionType.ANALOG_WORKFLOW, entity.getType());

    }
    @Test
    void getRecipientIndex() {
        entity.setRecipientIndex(1);
        Assertions.assertEquals(1, entity.getRecipientIndex());

    }
}