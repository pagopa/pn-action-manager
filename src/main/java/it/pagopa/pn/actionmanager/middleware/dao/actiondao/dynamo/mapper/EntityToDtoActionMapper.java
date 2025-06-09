package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@AllArgsConstructor
public class EntityToDtoActionMapper {
    private final SmartMapper smartMapper;

    public Action entityToDto(ActionEntity entity) {
        Action.ActionBuilder builder = Action.builder()
                .actionId(entity.getActionId())
                .notBefore(entity.getNotBefore())
                .recipientIndex(entity.getRecipientIndex())
                .createdAt(entity.getCreatedAt())
                .type(entity.getType())
                .timeslot(entity.getTimeslot())
                .timelineId(entity.getTimelineId())
                .iun(entity.getIun());

        if(entity.getDetails() != null && !entity.getDetails().isEmpty()) {
            builder.details(entityDetailsToDto(entity.getDetails()));
        }

        return builder.build();
    }
    
    private String entityDetailsToDto(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return "";
        }
        return smartMapper.mapFromMapToJsonString(details);
    }
}
