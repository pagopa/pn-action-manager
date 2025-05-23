package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper;

import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
@Component
@AllArgsConstructor
public class DtoToEntityActionMapper {
    private final SmartMapper smartMapper;

    public ActionEntity dtoToEntity(Action dto, Duration actionTtl) {
        ActionEntity.ActionEntityBuilder builder =  ActionEntity.builder()
                .actionId(dto.getActionId())
                .notBefore(dto.getNotBefore())
                .recipientIndex(dto.getRecipientIndex())
                .type(dto.getType())
                .timeslot(dto.getTimeslot())
                .timelineId(dto.getTimelineId())
                .iun(dto.getIun())
                .details(dtoToDetailsEntity(dto.getDetails()));

        if (!actionTtl.isZero())
            builder.ttl(LocalDateTime.now().plus(actionTtl).atZone(ZoneId.systemDefault()).toEpochSecond());

        return builder.build();
    }
    
    private  Map<String, Object> dtoToDetailsEntity(String details) {
      return smartMapper.mapFromStringToMap(details);
    }
}
