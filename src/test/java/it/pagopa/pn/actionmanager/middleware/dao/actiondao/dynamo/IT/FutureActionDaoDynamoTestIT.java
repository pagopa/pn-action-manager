package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.IT;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.pagopa.pn.actionmanager.config.BaseTest;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.FutureActionDaoDynamo;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;

@TestPropertySource(properties = {
        FutureActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
class FutureActionDaoDynamoTestIT extends BaseTest.WithLocalStack{
    @Autowired
    private FutureActionDaoDynamo futureActionDaoDynamo;

    @Test
    void unscheduleActionFailSilent_async() {

        String timeSlot = "2021-09-16T15:24";
        String actionId = "Test_unscheduleActionFailSilent_actionId";

        Logger fooLogger = (Logger) LoggerFactory.getLogger(FutureActionDaoDynamo.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);

        // Prima chiamata: inserisce l'azione
        StepVerifier.create(futureActionDaoDynamo.unscheduleAction(timeSlot, actionId))
                .verifyComplete();

        // Seconda chiamata: duplicato, deve loggare warn e non lanciare errori
        StepVerifier.create(futureActionDaoDynamo.unscheduleAction(timeSlot, actionId))
                .verifyComplete();

        // Verifica log
        List<ILoggingEvent> logsList = listAppender.list;

        String expectedMessage = "Exception code ConditionalCheckFailed on update future action, letting flow continue actionId=" + actionId;
        Assertions.assertEquals(expectedMessage, logsList.get(0).getFormattedMessage().trim());
        Assertions.assertEquals(Level.WARN, logsList.get(0).getLevel());
    }
}