package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.IT;

import it.pagopa.pn.actionmanager.LocalStackTestConfig;
import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.FutureActionDaoDynamo;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        FutureActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
@SpringBootTest
@Import(LocalStackTestConfig.class)
class FutureActionDaoDynamoTestIT {
    @Mock
    private DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    private DynamoDbEnhancedAsyncClient enhancedAsyncClient;
    @Autowired
    private FutureActionDaoDynamo futureActionDaoDynamo;
    @Mock
    private PnActionManagerConfigs pnActionManagerConfigs;

    @Test
    void unscheduleAction_notFound() {
        StepVerifier.create(futureActionDaoDynamo.unscheduleAction("not-exist", "not-exist"))
                .expectError(PnNotFoundException.class)
                .verify();
    }

    @Test
    void unscheduleAction_success() {
        FutureActionEntity entity = new FutureActionEntity();
        entity.setTimeSlot("slot1");
        entity.setActionId("act1");
        entity.setLogicalDeleted(false);

        enhancedAsyncClient.table("FutureAction", TableSchema.fromClass(FutureActionEntity.class))
                .putItem(entity).join();

        StepVerifier.create(futureActionDaoDynamo.unscheduleAction("slot1", "act1"))
                .verifyComplete();
    }
}