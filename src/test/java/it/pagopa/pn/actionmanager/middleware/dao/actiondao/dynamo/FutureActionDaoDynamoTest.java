package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.dto.ActionType;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FutureActionDaoDynamoTest {

    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient;

    @Mock
    private FutureActionDaoDynamo dynamo;

    @Mock
    private  DynamoDbAsyncTable<FutureActionEntity> table;

    @Mock
    private FutureActionDao futureActionDao;

    @BeforeEach
    void setup() {
        // Configura il mock per la configurazione
        PnActionManagerConfigs.FutureActionDao futureActionDao = new PnActionManagerConfigs.FutureActionDao();
        futureActionDao.setTableName("FutureAction");
        when(pnActionManagerConfigs.getFutureActionDao()).thenReturn(futureActionDao);

        when(dynamoDbEnhancedClient.table(
                Mockito.eq("FutureAction"),
                any(TableSchema.class)
        )).thenReturn(table);

        // Inizializza l'istanza reale con i mock
        dynamo = new FutureActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs);
    }

    @Test
    void testUnscheduleActionSuccess() {
        String timeslot = "2022-08-30T16:04:13.913859900Z";
        Action action = buildAction();
        FutureActionEntity expectedEntity = buildFutureActionEntity(action, timeslot);
        expectedEntity.setLogicalDeleted(true);

        // Esegui il metodo da testare
        dynamo.unscheduleAction(expectedEntity.getTimeSlot(), expectedEntity.getActionId());

        assertEquals(timeslot, expectedEntity.getTimeSlot());
        assertEquals(expectedEntity.getActionId(), action.getActionId());
        assertEquals(true, expectedEntity.getLogicalDeleted());

    }

    @Test
    void unscheduleActionError() {
        // Arrange
        String timeSlot = "2022-08-30T16:04:13.913859900Z";
        String actionId = "02";
        ConditionalCheckFailedException exception = ConditionalCheckFailedException.builder()
                .message("Condition check failed")
                .build();

        // Simula il comportamento del metodo updateItem per lanciare l'eccezione
        when(table.updateItem(any(Consumer.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // Act & Assert
        StepVerifier.create(dynamo.unscheduleAction(timeSlot, actionId))
                .expectErrorSatisfies(throwable -> {
                    assertTrue(throwable instanceof PnNotFoundException);
                    PnNotFoundException ex = (PnNotFoundException) throwable;
                    assertEquals("Not found", ex.getMessage());
                })
                .verify();
    }

    private Action buildAction() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        return Action.builder()
                .iun("01")
                .actionId("02")
                .logicalDeleted(false)
                .notBefore(instant)
                .type(ActionType.ANALOG_WORKFLOW)
                .recipientIndex(3)
                .build();
    }
    private FutureActionEntity buildFutureActionEntity(Action dto, String timeSlot) {
        FutureActionEntity.FutureActionEntityBuilder builder = FutureActionEntity.builder()
                .timeSlot(timeSlot)
                .actionId(dto.getActionId())
                .notBefore(dto.getNotBefore())
                .logicalDeleted(dto.getLogicalDeleted())
                .recipientIndex(dto.getRecipientIndex())
                .type(dto.getType())
                .iun(dto.getIun());
        return builder.build();
    }
}