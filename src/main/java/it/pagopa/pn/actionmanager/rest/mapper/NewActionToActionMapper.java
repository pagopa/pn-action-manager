package it.pagopa.pn.actionmanager.rest.mapper;

import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.dto.action.CommunicationType;
import it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.NewAction;
import jakarta.validation.Valid;

public class NewActionToActionMapper {
    public static Action map(NewAction newAction) {
        return Action.builder()
                .iun(newAction.getIun())
                .actionId(newAction.getActionId())
                .notBefore(newAction.getNotBefore())
                .type(ActionType.valueOf(newAction.getType().getValue()))
                .recipientIndex(newAction.getRecipientIndex())
                .timelineId(newAction.getTimelineId())
                .details(newAction.getDetails())
                .communicationType(mapCommunicationType(newAction.getCommunicationType()))
                .build();
    }

    private static CommunicationType mapCommunicationType(it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.@Valid CommunicationType communicationType) {
        if (communicationType == null) {
            return CommunicationType.LEGAL;
        }

        return CommunicationType.valueOf(communicationType.getValue());
    }
}
