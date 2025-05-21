package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.IT;

import it.pagopa.pn.actionmanager.LocalStackTestConfig;
import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.ActionDaoDynamo;
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
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import java.time.Instant;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {
        ActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME + "=" + MiddlewareTypes.DYNAMO
})
@SpringBootTest
@Import(LocalStackTestConfig.class)
class ActionDaoDynamoTestIT {

    @Mock
    private DynamoDbAsyncClient dynamoDbAsyncClient;
    @Mock
    private DynamoDbEnhancedAsyncClient enhancedAsyncClient;
    @Autowired
    private ActionDaoDynamo actionDaoDynamo;
    @Mock
    private PnActionManagerConfigs configs;

    @Test
    void addOnlyActionIfAbsent_success() {
        Action action = Action.builder()
                .iun("IT1")
                .actionId("A1")
                .notBefore(Instant.now())
                .type(ActionType.ANALOG_WORKFLOW)
                .recipientIndex(1)
                .build();

        StepVerifier.create(actionDaoDynamo.addOnlyActionIfAbsent(action))
                .verifyComplete();
    }

    @Test
    void addOnlyActionIfAbsent_duplicate() {
        Action action = Action.builder()
                .iun("IT2")
                .actionId("A2")
                .notBefore(Instant.now())
                .type(ActionType.ANALOG_WORKFLOW)
                .recipientIndex(2)
                .build();

        // Prima inserzione
        StepVerifier.create(actionDaoDynamo.addOnlyActionIfAbsent(action))
                .verifyComplete();

        // Seconda inserzione (duplicato)
        StepVerifier.create(actionDaoDynamo.addOnlyActionIfAbsent(action))
                .verifyComplete(); // Deve completare senza errori
    }
}