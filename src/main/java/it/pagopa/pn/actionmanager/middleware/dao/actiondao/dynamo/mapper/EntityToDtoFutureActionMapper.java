package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;

//TODO: remove this class when the ActionDetailsEntity will be removed
public class EntityToDtoFutureActionMapper {
    private EntityToDtoFutureActionMapper(){}
    
    public static Action entityToDto(FutureActionEntity entity) {
        Action.ActionBuilder builder = Action.builder()
                .actionId(entity.getActionId())
                .notBefore(entity.getNotBefore())
                .recipientIndex(entity.getRecipientIndex())
                .logicalDeleted(entity.getLogicalDeleted())
                .type(entity.getType())
                .timelineId(entity.getTimelineId())
                .details("")
                .iun(entity.getIun());
        return builder.build();
    }
}
