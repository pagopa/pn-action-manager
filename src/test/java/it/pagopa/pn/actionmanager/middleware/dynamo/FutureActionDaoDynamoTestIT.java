package it.pagopa.pn.actionmanager.middleware.dynamo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.List;

@TestPropertySource(properties = {
        FutureActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
class FutureActionDaoDynamoTestIT extends BaseTest.WithLocalStack{
    @Autowired
    private FutureActionDao futureActionDao;

    @Test
    void addOnlyActionIfAbsentFailSilent_async() {

        String timeSlot = "2021-09-16T15:24";
        String actionId = "Test_unscheduleActionFailSilent_actionId";

        ch.qos.logback.classic.Logger fooLogger = (Logger) LoggerFactory.getLogger(FutureActionDaoDynamo.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);

        StepVerifier.create(futureActionDao.unscheduleAction(timeSlot, actionId))
                .verifyError(PnNotFoundException.class);

        // Verifica log
        List<ILoggingEvent> logsList = listAppender.list;

        String expectedMessage = "Exception code ConditionalCheckFailed on update future action, letting flow continue actionId=" + actionId;
        Assertions.assertEquals(expectedMessage, logsList.get(0).getFormattedMessage().trim());
        Assertions.assertEquals(Level.ERROR, logsList.get(0).getLevel());
    }

}