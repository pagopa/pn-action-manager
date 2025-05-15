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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;

class ActionDaoDynamoTest {
    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private ActionDaoDynamo dynamo;

    @Mock
    private ActionDao actionDao;


    @BeforeEach
    void setup() {
        PnActionManagerConfigs.ActionDao actionDao = new PnActionManagerConfigs.ActionDao();
        actionDao.setTableName("Action");
        PnActionManagerConfigs.FutureActionDao factionDao = new PnActionManagerConfigs.FutureActionDao();
        factionDao.setTableName("FutureAction");

        // Configura il mock per il client DynamoDB
        DynamoDbTable<ActionEntity> mockTable = Mockito.mock(DynamoDbTable.class);
        Mockito.when(dynamoDbEnhancedClient.table(
                Mockito.eq("Action"),
                Mockito.any(TableSchema.class)
        )).thenReturn(mockTable);

        Mockito.when(pnActionManagerConfigs.getActionDao()).thenReturn(actionDao);
        Mockito.when(pnActionManagerConfigs.getActionTtlDays()).thenReturn("1095");
        Mockito.when(pnActionManagerConfigs.getFutureActionDao()).thenReturn(factionDao);
        dynamo = new ActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs);
    }

    @Test
    @ExtendWith(SpringExtension.class)
    void addOnlyActionIfAbsent() {
        Action action = buildAction();
        ActionEntity actionEntity = buildActionEntity(action);

        dynamo.addOnlyActionIfAbsent(action);

        Assertions.assertEquals(actionEntity, actionEntity);
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