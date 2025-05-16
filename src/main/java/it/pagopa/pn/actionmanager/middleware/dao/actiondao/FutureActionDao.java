package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import reactor.core.publisher.Mono;

public interface FutureActionDao {
    String IMPLEMENTATION_TYPE_PROPERTY_NAME = "pn.middleware.impl.future-action-dao";

    Mono<Void> unscheduleAction(String timeSlot, String actionId);
}
