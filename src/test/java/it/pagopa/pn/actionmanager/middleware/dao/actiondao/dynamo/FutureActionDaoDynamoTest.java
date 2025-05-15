package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;

class FutureActionDaoDynamoTest {

    private FutureActionDaoDynamo actionEntityDaoDynamo;
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    private PnActionManagerConfigs cfg;

    @BeforeEach
    public void setup(){
        cfg = Mockito.mock(PnActionManagerConfigs.class);
        dynamoDbEnhancedClient = Mockito.mock(DynamoDbEnhancedClient.class);

        Mockito.when(cfg.getFutureActionDao()).thenReturn(Mockito.mock(PnActionManagerConfigs.FutureActionDao.class));

        actionEntityDaoDynamo = new FutureActionDaoDynamo(dynamoDbEnhancedClient, cfg);
    }
}