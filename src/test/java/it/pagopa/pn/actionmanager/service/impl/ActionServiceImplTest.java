package it.pagopa.pn.actionmanager.service.impl;


import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class ActionServiceImplTest {

    @Mock
    private ActionDao actionDao;

    private FutureActionDao futureActionDao;

    private ActionServiceImpl actionService;

    @BeforeEach
    public void setup() {
        actionDao = Mockito.mock(ActionDao.class);
        futureActionDao = Mockito.mock(FutureActionDao.class);
        PnActionManagerConfigs pnActionManagerConfigs = new PnActionManagerConfigs();
        pnActionManagerConfigs.setDetailsMaxSizeBytes(10000);
        pnActionManagerConfigs.setDetailsMaxDepth(5);
        actionService = new ActionServiceImpl(actionDao, futureActionDao, pnActionManagerConfigs);
    }

    @Test
    void unscheduleAction(){
        String timeSlot = "2021-09-16T15:24";
        String actionId = "001";

        Mockito.when(futureActionDao.unscheduleAction(timeSlot, actionId)).thenReturn(Mono.empty());

        StepVerifier.create(actionService.unscheduleAction(timeSlot, actionId))
                .verifyComplete();

        // Verifica che il DAO sia stato chiamato con l'oggetto corretto
        Mockito.verify(futureActionDao, Mockito.times(1)).unscheduleAction(timeSlot, actionId);
    }

    @Test
    void addOnlyActionIfAbsent() {
        Action action = buildAction();

        // Calcola il valore atteso di timeslot
        String expectedTimeSlot = "2021-09-16T15:24";
        Action expectedAction = action.toBuilder()
                .timeslot(expectedTimeSlot)
                .build();

        Mockito.when(actionDao.addOnlyActionIfAbsent(expectedAction)).thenReturn(Mono.empty());

        StepVerifier.create(actionService.addOnlyActionIfAbsent(action))
                .verifyComplete();

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