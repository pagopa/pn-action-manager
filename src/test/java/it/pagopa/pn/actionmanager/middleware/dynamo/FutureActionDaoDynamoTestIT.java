package it.pagopa.pn.actionmanager.middleware.dynamo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.pagopa.pn.actionmanager.LocalStackTestConfig;
import it.pagopa.pn.actionmanager.config.BaseTest;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.FutureActionDaoDynamo;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@TestPropertySource(properties = {
        FutureActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
class FutureActionDaoDynamoTestIT extends BaseTest.WithLocalStack{
    @Autowired
    private FutureActionDao futureActionDao;

    @Test
    void unscheduleActionFailSilent() {
        String timeSlot = "2021-09-16T15:24";
        String actionId = "Test_unscheduleActionFailSilent_actionId";

        // Ottieni il logger di Logback
        Logger fooLogger = LoggerFactory.getLogger(FutureActionDaoDynamo.class);

        // Crea e avvia un ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        // Aggiungi l'appender al logger
        ((ch.qos.logback.classic.Logger) fooLogger).addAppender(listAppender);

        // Verifica che il metodo non lanci eccezioni
        Assertions.assertThrows(
                PnNotFoundException.class,
                () -> futureActionDao.unscheduleAction(timeSlot, actionId)
        );

        // Recupera i log registrati
        List<ILoggingEvent> logsList = listAppender.list;

        // Verifica che il messaggio di log sia corretto
        String expectedMessage = String.format("Action not found for timeSlot=%s and actionId=%s", timeSlot, actionId);
        Assertions.assertEquals(expectedMessage, logsList.get(0).getFormattedMessage());
        Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
    }

}