package it.pagopa.pn.actionmanager.service;


import it.pagopa.pn.actionmanager.dto.action.Action;
import reactor.core.publisher.Mono;

public interface ActionService {

    Mono<Void> addOnlyActionIfAbsent(Action action);
    Mono<Void> unscheduleAction(String timeSlot, String actionId);
}
