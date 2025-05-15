package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class FutureActionDaoDynamoTest {

    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private FutureActionDaoDynamo dynamo;

    @Mock
    private FutureActionDao futureActionDao;

    @BeforeEach
    void setup() {
        // Configura il mock per la configurazione
        PnActionManagerConfigs.FutureActionDao futureActionDao = new PnActionManagerConfigs.FutureActionDao();
        futureActionDao.setTableName("FutureAction");
        Mockito.when(pnActionManagerConfigs.getFutureActionDao()).thenReturn(futureActionDao);

        // Configura il mock per il client DynamoDB
        DynamoDbTable<FutureActionEntity> mockTable = Mockito.mock(DynamoDbTable.class);
        Mockito.when(dynamoDbEnhancedClient.table(
                Mockito.eq("FutureAction"),
                Mockito.any(TableSchema.class)
        )).thenReturn(mockTable);

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