package it.pagopa.pn.actionmanager.service.impl;


import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.service.ActionService;
import it.pagopa.pn.actionmanager.utils.DetailsValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Slf4j
@AllArgsConstructor
@Service
public class ActionServiceImpl implements ActionService {
    private final ActionDao actionDao;
    private final FutureActionDao futureActionDao;
    private final PnActionManagerConfigs pnActionManagerConfigs;

    @Override
    public Mono<Void> addOnlyActionIfAbsent(Action action) {
        log.info("addOnlyActionIfAbsent actionId={} action={}", action.getActionId(), action);
        final String timeSlot = computeTimeSlot( action.getNotBefore() );
        action = action.toBuilder()
                .timeslot( timeSlot)
                .build();
        DetailsValidationUtils.validateDetails(action.getDetails(), pnActionManagerConfigs.getDetailsMaxSizeBytes(), pnActionManagerConfigs.getDetailsMaxDepth());
        return actionDao.addOnlyActionIfAbsent(action);
    }

    private String computeTimeSlot(Instant instant) {
        OffsetDateTime nowUtc = instant.atOffset(ZoneOffset.UTC);
        return String.format("%04d-%02d-%02dT%02d:%02d",
                nowUtc.getYear(),
                nowUtc.getMonthValue(),
                nowUtc.getDayOfMonth(),
                nowUtc.getHour(),
                nowUtc.getMinute());
    }

    @Override
    public Mono<Void> unscheduleAction(String timeSlot, String actionId){
        log.info("unscheduleAction timeSlot={} actionId={}", timeSlot, actionId);
        return futureActionDao.unscheduleAction(timeSlot, actionId);
    }
}
