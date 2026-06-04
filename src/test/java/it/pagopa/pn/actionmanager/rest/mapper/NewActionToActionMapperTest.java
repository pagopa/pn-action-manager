package it.pagopa.pn.actionmanager.rest.mapper;

import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.dto.action.CommunicationType;
import it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.NewAction;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class NewActionToActionMapperTest {
    @Test
    void mapsAllFieldsCorrectly() {
        it.pagopa.pn.actionmanager.dto.action.ActionType expectedType = ActionType.ANALOG_WORKFLOW;
        CommunicationType expectedCommunicationType = CommunicationType.INFORMAL;
        NewAction newAction = new NewAction();
        newAction.setIun("IUN_123");
        newAction.setActionId("ACTION_456");
        newAction.setNotBefore(Instant.parse("2025-01-01T10:15:30Z"));
        newAction.setType(it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.ActionType.valueOf(expectedType.name()));
        newAction.setRecipientIndex(2);
        newAction.setTimelineId("TIMELINE_789");
        newAction.setCommunicationType(
             it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.CommunicationType.valueOf(
                expectedCommunicationType.name()
             )
        );

        Action result = NewActionToActionMapper.map(newAction);

        assertNotNull(result);
        assertEquals("IUN_123", result.getIun());
        assertEquals("ACTION_456", result.getActionId());
        assertEquals("2025-01-01T10:15:30Z", result.getNotBefore().toString());
        assertEquals(expectedType, result.getType());
        assertEquals(2, result.getRecipientIndex());
        assertEquals("TIMELINE_789", result.getTimelineId());
        assertEquals(expectedCommunicationType, result.getCommunicationType());
    }

    @Test
    void defaultsCommunicationTypeToLegalWhenMissing() {
        it.pagopa.pn.actionmanager.dto.action.ActionType expectedType =
                it.pagopa.pn.actionmanager.dto.action.ActionType.values()[0];

        NewAction newAction = new NewAction();
        newAction.setIun("IUN_123");
        newAction.setActionId("ACTION_456");
        newAction.setType(it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.ActionType.valueOf(expectedType.name()));
        newAction.setCommunicationType(null);

        Action result = NewActionToActionMapper.map(newAction);

        assertEquals(it.pagopa.pn.actionmanager.dto.action.CommunicationType.LEGAL, result.getCommunicationType());
    }

    @Test
    void throwsNullPointerExceptionWhenTypeIsNull() {
        NewAction newAction = new NewAction();
        newAction.setIun("IUN_123");
        newAction.setActionId("ACTION_456");
        newAction.setType(null);

        assertThrows(NullPointerException.class, () -> NewActionToActionMapper.map(newAction));
    }
}