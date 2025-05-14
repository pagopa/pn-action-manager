package it.pagopa.pn.actionmanager.service.impl;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;

import it.pagopa.pn.actionmanager.service.ActionService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

@Slf4j
@AllArgsConstructor
@Service
public class ActionServiceImpl implements ActionService {
    private final ActionDao actionDao;

    @Override
    public void addOnlyActionIfAbsent(Action action) {
        final String timeSlot = computeTimeSlot( action.getNotBefore() );
        action = action.toBuilder()
                .timeslot( timeSlot)
                .build();
        actionDao.addOnlyActionIfAbsent(action);
    }

    @Override
    public void unscheduleAction(String timeSlot, String actionId){
        actionDao.unscheduleAction(timeSlot, actionId);
    }

    private String computeTimeSlot(Instant instant) {
        OffsetDateTime nowUtc = instant.atOffset( ZoneOffset.UTC );
        int year = nowUtc.get( ChronoField.YEAR_OF_ERA );
        int month = nowUtc.get( ChronoField.MONTH_OF_YEAR );
        int day = nowUtc.get( ChronoField.DAY_OF_MONTH );
        int hour = nowUtc.get( ChronoField.HOUR_OF_DAY );
        int minute = nowUtc.get( ChronoField.MINUTE_OF_HOUR );
        return String.format("%04d-%02d-%02dT%02d:%02d", year, month, day, hour, minute);
    }
}
