package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import reactor.core.publisher.Mono;

public interface FutureActionDao {
    Mono<Void> unscheduleAction(String timeSlot, String actionId);
}
