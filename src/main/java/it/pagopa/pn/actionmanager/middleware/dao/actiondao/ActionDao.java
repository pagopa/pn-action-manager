package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import it.pagopa.pn.actionmanager.dto.action.Action;

public interface ActionDao {
    String IMPLEMENTATION_TYPE_PROPERTY_NAME = "pn.middleware.impl.action-dao";

    void addOnlyActionIfAbsent(Action action);
    void unscheduleAction(String timeSlot, String actionId);
}
