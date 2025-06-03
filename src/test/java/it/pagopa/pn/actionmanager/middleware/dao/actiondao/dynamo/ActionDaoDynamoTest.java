package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.exceptions.PnConflictException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.DtoToEntityActionMapper;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.EntityToDtoActionMapper;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class ActionDaoDynamoTest {
    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedAsyncClient dynamoDbEnhancedClient;

    @Mock
    private DynamoDbAsyncTable<ActionEntity> table;

    @Mock
    private ActionDaoDynamo actionDaoDynamo;

    @BeforeEach
    void setup() {
        PnActionManagerConfigs.ActionDao actionDao = new PnActionManagerConfigs.ActionDao();
        actionDao.setTableName("Action");
        pnActionManagerConfigs = Mockito.mock(PnActionManagerConfigs.class);

        // Configura il mock per il client DynamoDB
        table = Mockito.mock(DynamoDbAsyncTable.class);
        dynamoDbEnhancedClient = Mockito.mock(DynamoDbEnhancedAsyncClient.class);
        when(dynamoDbEnhancedClient.table(
                Mockito.eq("Action"),
                Mockito.any(TableSchema.class)
        )).thenReturn(table);

        when(pnActionManagerConfigs.getActionDao()).thenReturn(actionDao);
        when(pnActionManagerConfigs.getActionTtlDays()).thenReturn("1095");

        SmartMapper smartMapper = Mockito.mock(SmartMapper.class);
        when(smartMapper.mapFromJsonStringToMap(Mockito.anyString()))
                .thenReturn(new HashMap<>());
        DtoToEntityActionMapper dtoToEntityActionMapper = new DtoToEntityActionMapper(smartMapper);
        EntityToDtoActionMapper entityToDtoActionMapper = new EntityToDtoActionMapper(smartMapper);

        actionDaoDynamo = new ActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs, dtoToEntityActionMapper, entityToDtoActionMapper);
    }

    @Test
    void addOnlyActionIfAbsent() {
        Action action = buildAction();

        when(table.putItem(Mockito.any(PutItemEnhancedRequest.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(actionDaoDynamo.addOnlyActionIfAbsent(action))
                .verifyComplete();
    }

    @Test
    void addOnlyActionIfAbsentError() {
        // Arrange
        Action action = buildAction();
        ConditionalCheckFailedException exception = ConditionalCheckFailedException.builder().message("Action already exists").build();

        // Simula il comportamento del metodo putItem per lanciare l'eccezione
        when(table.putItem(Mockito.any(PutItemEnhancedRequest.class)))
                .thenReturn(CompletableFuture.failedFuture(exception));

        StepVerifier.create(actionDaoDynamo.addOnlyActionIfAbsent(action))
                .expectErrorSatisfies(throwable -> {
                    Assertions.assertInstanceOf(PnConflictException.class, throwable);
                    PnConflictException ex = (PnConflictException) throwable;
                    assertEquals("Conflict", ex.getMessage());
                })
                .verify();
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

    @Test
    void getAction() {
        Action action = buildAction();
        ActionEntity actionEntity = new ActionEntity();
        Map<String, Object> details = new HashMap<>();
        details.put("key", "value");
        actionEntity.setActionId("02");
        actionEntity.setDetails(details);

        when(table.getItem(Mockito.any(Key.class)))
                .thenReturn(CompletableFuture.completedFuture(actionEntity));

        StepVerifier.create(actionDaoDynamo.getAction("02"))
                .expectNextMatches(retrievedAction -> {
                    assertEquals(action.getActionId(), retrievedAction.getActionId());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getActionNotFound() {
        when(table.getItem(Mockito.any(Key.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        StepVerifier.create(actionDaoDynamo.getAction("non-existent-action-id"))
                .expectErrorMessage("Not found")
                .verify();
    }
}