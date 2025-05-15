package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

public interface FutureActionDao {
    String IMPLEMENTATION_TYPE_PROPERTY_NAME = "pn.middleware.impl.future-action-dao";

    void unscheduleAction(String timeSlot, String actionId);
}
