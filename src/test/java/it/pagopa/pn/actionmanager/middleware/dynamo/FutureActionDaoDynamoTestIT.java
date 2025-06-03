package it.pagopa.pn.actionmanager.middleware.dynamo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.pagopa.pn.actionmanager.config.BaseTest;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.FutureActionDaoDynamo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;

import java.util.List;

class FutureActionDaoDynamoTestIT extends BaseTest.WithLocalStack {
    @Autowired
    private FutureActionDao futureActionDao;

    @Test
    void unscheduleActionFail() {
        String timeSlot = "2021-09-16T15:24";
        String actionId = "Test_unscheduleActionFailSilent_actionId";

        // Ottieni il logger di Logback
        Logger fooLogger = LoggerFactory.getLogger(FutureActionDaoDynamo.class);

        // Crea e avvia un ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        // Aggiungi l'appender al logger
        ((ch.qos.logback.classic.Logger) fooLogger).addAppender(listAppender);

        // Prova a disschedulare un'azione non esistente
        StepVerifier.create(futureActionDao.unscheduleAction(timeSlot, actionId))
                .verifyError(PnNotFoundException.class);

        // Recupera i log registrati
        List<ILoggingEvent> logsList = listAppender.list;

        // Verifica che il messaggio di log sia corretto
        String expectedMessage = String.format("Future action with actionId=%s and timeSlot=%s not found", actionId, timeSlot);
        Assertions.assertEquals(expectedMessage, logsList.getFirst().getFormattedMessage());
        Assertions.assertEquals(Level.INFO, logsList.getFirst().getLevel());
    }

}