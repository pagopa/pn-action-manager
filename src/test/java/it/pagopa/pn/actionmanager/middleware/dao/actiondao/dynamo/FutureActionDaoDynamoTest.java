package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import org.junit.jupiter.api.Assertions;
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

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FutureActionDaoDynamoTest {

    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient;

    @Mock
    private DynamoDbAsyncTable<FutureActionEntity> table;

    @Mock
    private FutureActionDaoDynamo futureActionDaoDynamo;

    @BeforeEach
    void setup() {
        // Configura il mock per la configurazione
        PnActionManagerConfigs.FutureActionDao futureActionDao = new PnActionManagerConfigs.FutureActionDao();
        futureActionDao.setTableName("FutureAction");
        when(pnActionManagerConfigs.getFutureActionDao()).thenReturn(futureActionDao);

        dynamoDbEnhancedClient = Mockito.mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedClient.table(
                Mockito.eq("FutureAction"),
                any(TableSchema.class)
        )).thenReturn(table);

        // Inizializza l'istanza reale con i mock
        futureActionDaoDynamo = new FutureActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs);
    }

    @Test
    void testUnscheduleActionSuccess() {
        String timeslot = "2022-08-30T16:04:13.913859900Z";
        String actionId = "02";

        when(table.updateItem(any(Consumer.class)))
                .thenReturn(CompletableFuture.completedFuture(Void.class));

        StepVerifier.create(futureActionDaoDynamo.unscheduleAction(timeslot, actionId))
                .verifyComplete();

        verify(table, times(1)).updateItem(any(Consumer.class));
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
        StepVerifier.create(futureActionDaoDynamo.unscheduleAction(timeSlot, actionId))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnNotFoundException.class, throwable);
                    PnNotFoundException ex = (PnNotFoundException) throwable;
                    assertEquals("Not found", ex.getMessage());
                })
                .verify();
    }
}