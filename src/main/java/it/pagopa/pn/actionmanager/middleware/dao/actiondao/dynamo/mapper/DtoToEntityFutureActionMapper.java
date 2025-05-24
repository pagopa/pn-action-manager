package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
//TODO: remove this class
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
                .details(null);
        return builder.build();
    }
}
