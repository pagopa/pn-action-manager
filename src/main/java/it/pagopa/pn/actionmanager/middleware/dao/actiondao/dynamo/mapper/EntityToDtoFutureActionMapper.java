package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionDetails;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionDetailsEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;

import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
                .details(parseDetailsFromEntity(entity.getDetails(),entity.getType()))
                .iun(entity.getIun());
        return builder.build();
    }
    
    private static ActionDetails parseDetailsFromEntity(ActionDetailsEntity entity, ActionType type) {
      log.info("EntityToDtoFutureActionMapper.parseDetailsFromEntity: {}", entity);
      return SmartMapper.mapToClass(entity, type.getDetailsJavaClass());
    }
}
