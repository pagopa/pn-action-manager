package it.pagopa.pn.actionmanager.service.impl;


import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class ActionServiceImplTest {

    @Mock
    private ActionDao actionDao;

    @Mock
    private FutureActionDao futureActionDao;

    private ActionServiceImpl actionService;

    @BeforeEach
    public void setup() {
        actionDao = Mockito.mock(ActionDao.class);
        actionService = new ActionServiceImpl(actionDao, futureActionDao);
    }

    @Test
    void unscheduleAction(){
        String timeSlot = "2021-09-16T15:24";
        String actionId = "001";
        actionService.unscheduleAction(actionId, timeSlot);

        // Verifica che il DAO sia stato chiamato con l'oggetto corretto
        Mockito.verify(futureActionDao, Mockito.times(1)).unscheduleAction(Mockito.any(), Mockito.eq(timeSlot));
    }

    @Test
    void addOnlyActionIfAbsent() {
        Action action = buildAction();

        // Calcola il valore atteso di timeslot
        String expectedTimeSlot = "2021-09-16T15:24";
        Action expectedAction = action.toBuilder()
                .timeslot(expectedTimeSlot)
                .build();

        actionService.addOnlyActionIfAbsent(action);

        // Verifica che il DAO sia stato chiamato con l'oggetto corretto
        Mockito.verify(actionDao, Mockito.times(1)).addOnlyActionIfAbsent(expectedAction);
    }

    private Action buildAction() {
        Instant instant = Instant.parse("2021-09-16T15:24:00.00Z");

        return Action.builder()
                .recipientIndex(0)
                .iun("001")
                .type(ActionType.ANALOG_WORKFLOW)
                .actionId("002")
                .notBefore(instant)
                .build();
    }

}