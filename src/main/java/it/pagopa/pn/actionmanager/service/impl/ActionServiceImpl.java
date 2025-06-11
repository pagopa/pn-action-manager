package it.pagopa.pn.actionmanager.service.impl;


import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.exceptions.PnBadRequestException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.service.ActionService;
import it.pagopa.pn.actionmanager.utils.DetailsValidationUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.utils.StringUtils;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTION_BAD_REQUEST;

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
        validateActionIdAndIun(action);
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
    public Mono<Void> unscheduleAction( String actionId){
        log.info("unscheduleAction actionId={}", actionId);
        return getAction(actionId)
                .flatMap(r -> futureActionDao.unscheduleAction(r.getTimeslot(), actionId));
    }

    private Mono<Action> getAction(String actionId) {
        log.info("getAction actionId={}", actionId);
        return actionDao.getAction(actionId);
    }
    private void validateActionIdAndIun(Action action) {
        if (StringUtils.isBlank(action.getActionId())) {
            String message = "actionId cannot be blank";
            log.error(message);
            throw new PnBadRequestException("actionId cannot be blank", message, ERROR_CODE_ACTION_BAD_REQUEST);
        }
        if (StringUtils.isBlank(action.getIun())) {
            String message = "iun cannot be blank";
            log.error(message);
            throw new PnBadRequestException("iun cannot be blank", message, ERROR_CODE_ACTION_BAD_REQUEST);
        }
    }
}
