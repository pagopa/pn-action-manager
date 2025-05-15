package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.Mockito;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

class ActionDaoDynamoTest {
    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Mock
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;

    @Mock
    private ActionDaoDynamo dynamo;

    @BeforeEach
    void setup() {
        PnActionManagerConfigs.ActionDao actionDao = new PnActionManagerConfigs.ActionDao();
        actionDao.setTableName("Action");
        PnActionManagerConfigs.FutureActionDao factionDao = new PnActionManagerConfigs.FutureActionDao();
        factionDao.setTableName("FutureAction");
        Mockito.when(pnActionManagerConfigs.getActionDao()).thenReturn(actionDao);
        Mockito.when(pnActionManagerConfigs.getActionTtlDays()).thenReturn("1095");
        Mockito.when(pnActionManagerConfigs.getFutureActionDao()).thenReturn(factionDao);
        dynamo = new ActionDaoDynamo(dynamoDbEnhancedClient, pnActionManagerConfigs);
    }

}