package it.pagopa.pn.actionmanager.middleware.dynamo;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import it.pagopa.pn.actionmanager.config.BaseTest;
import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.dto.ActionType;
import it.pagopa.pn.actionmanager.exceptions.PnConflictException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.ActionDaoDynamo;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.List;

@TestPropertySource(properties = {
        ActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
class ActionDaoDynamoTestIT extends BaseTest.WithLocalStack{
    @Autowired
    private ActionDao actionDao;

    @Test
    void addOnlyActionIfAbsentFailSilent_async() {
        Action.ActionBuilder actionBuilder = Action.builder()
                .iun("Test_addIfAbsentFailSilent_iun01")
                .recipientIndex(1)
                .details("{\"key\":\"TEST_KEY\",\"documentCreationType\":\"AAR\",\"timelineId\":\"TEST_TIMELINE_ID\"}")
                .type(ActionType.DIGITAL_WORKFLOW_RETRY_ACTION);
        String actionId = "Test_addIfAbsentFailSilent_iun01_digital_workflow_e_1_timelineid_";
        Action action = actionBuilder.actionId(actionId).build();

        Logger fooLogger = (Logger) LoggerFactory.getLogger(ActionDaoDynamo.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        fooLogger.addAppender(listAppender);

        // Prima chiamata: inserisce l'azione
        StepVerifier.create(actionDao.addOnlyActionIfAbsent(action))
                .verifyComplete();

        // Seconda chiamata: duplicato, deve loggare warn e non lanciare errori
        StepVerifier.create(actionDao.addOnlyActionIfAbsent(action))
                .verifyError(PnConflictException.class);
        // Verifica log
        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertFalse(logsList.isEmpty());
        Assertions.assertTrue(
                logsList.stream().anyMatch(
                        log -> log.getLevel() == Level.ERROR &&
                                log.getFormattedMessage().contains("Action already present in the table, actionId=" + actionId)
                )
        );
    }
}