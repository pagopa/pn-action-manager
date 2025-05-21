package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
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
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActionDaoDynamoTest {
    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient;

    @Mock
    private ActionDaoDynamo dynamo;

    @Mock
    private  DynamoDbAsyncTable<ActionEntity> table;

    @Mock
    private ActionDao actionDao;


    @BeforeEach
    void setup() {
        PnActionManagerConfigs.ActionDao actionDao = new PnActionManagerConfigs.ActionDao();
        actionDao.setTableName("Action");
        PnActionManagerConfigs.FutureActionDao factionDao = new PnActionManagerConfigs.FutureActionDao();
        factionDao.setTableName("FutureAction");

        // Configura il mock per il client DynamoDB
        when(dynamoDbEnhancedClient.table(
                Mockito.eq("Action"),
                Mockito.any(TableSchema.class)
        )).thenReturn(table);

        when(pnActionManagerConfigs.getActionDao()).thenReturn(actionDao);
        when(pnActionManagerConfigs.getActionTtlDays()).thenReturn("1095");

        dynamo = new ActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs);
    }

    @Test
    void addOnlyActionIfAbsent() {
        Action action = buildAction();
        dynamo.addOnlyActionIfAbsent(action);
        Assertions.assertNotNull(action);
    }

    @Test
    void addOnlyActionIfAbsentError() {
        // Arrange
        Action action = buildAction();
        ConditionalCheckFailedException exception = ConditionalCheckFailedException.builder().message("Action already exists").build();

        // Simula il comportamento del metodo putItem per lanciare l'eccezione
        when(table.putItem(Mockito.any(PutItemEnhancedRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        // Act & Assert
        StepVerifier.create(dynamo.addOnlyActionIfAbsent(action))
                .verifyComplete(); // Verifica che il Mono completi senza errori
    }

    private Action buildAction() {

        Instant instant = Instant.parse("2022-08-30T16:04:13.913859900Z");

        return Action.builder()
                .iun("01")
                .actionId("02")
                .notBefore(instant)
                .type(ActionType.ANALOG_WORKFLOW)
                .recipientIndex(3)
                .build();
    }

    private ActionEntity buildActionEntity(Action dto) {
        ActionEntity.ActionEntityBuilder builder = ActionEntity.builder()
                .actionId(dto.getActionId())
                .notBefore(dto.getNotBefore())
                .recipientIndex(dto.getRecipientIndex())
                .type(dto.getType())
                .iun(dto.getIun());
        return builder.build();
    }

}