package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;

//TODO remove this class when the ActionDetailsEntity is removed
public class EntityToDtoActionMapper {
    private EntityToDtoActionMapper(){}
    
    public static Action entityToDto(ActionEntity entity) {
        Action.ActionBuilder builder = Action.builder()
                .actionId(entity.getActionId())
                .notBefore(entity.getNotBefore())
                .recipientIndex(entity.getRecipientIndex())
                .type(entity.getType())
                .timelineId(entity.getTimelineId())
                .timeslot(entity.getTimeslot())
                .iun(entity.getIun())
                .details("");
        return builder.build();
        
    }
}
