package it.pagopa.pn.actionmanager.service;


import it.pagopa.pn.actionmanager.dto.action.Action;

public interface ActionService {

    void addOnlyActionIfAbsent(Action action);
    void unscheduleAction(String timeSlot, String actionId);
}
