package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;

import java.util.Map;

public class DtoToEntityFutureActionMapper {
    private DtoToEntityFutureActionMapper() {
    }

    public static FutureActionEntity dtoToEntity(Action dto, String timeSlot) {
        FutureActionEntity.FutureActionEntityBuilder builder = FutureActionEntity.builder()
                .timeSlot(timeSlot)
                .actionId(dto.getActionId())
                .notBefore(dto.getNotBefore())
                .recipientIndex(dto.getRecipientIndex())
                .logicalDeleted(dto.getLogicalDeleted())
                .type(dto.getType())
                .timelineId(dto.getTimelineId())
                .iun(dto.getIun())
                .details(dtoToDetailsEntity(dto.getDetails()));
        return builder.build();
    }
    
    private static Map<String, Object> dtoToDetailsEntity(String details) {
      return SmartMapper.mapFromStringToMap(details );
    }
}
