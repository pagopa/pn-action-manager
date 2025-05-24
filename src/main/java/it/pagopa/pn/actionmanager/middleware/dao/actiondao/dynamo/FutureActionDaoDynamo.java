package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_FUTURE_ACTION_NOTFOUND;

@Component
@Slf4j
public class FutureActionDaoDynamo  implements FutureActionDao {
    private final DynamoDbAsyncTable<FutureActionEntity> table;

    protected FutureActionDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, PnActionManagerConfigs cfg) {
        this.table = dynamoDbEnhancedAsyncClient.table(cfg.getFutureActionDao().getTableName(), TableSchema.fromClass(FutureActionEntity.class));
    }

    @Override
    public Mono<Void> unscheduleAction(String timeSlot, String actionId) {
        String keyConditionExpression = String.format("%s = :timeSlot AND %s = :actionId",
                FutureActionEntity.FIELD_TIME_SLOT, FutureActionEntity.FIELD_ACTION_ID);

        Expression conditionExpressionUpdate = Expression.builder()
                .expression(keyConditionExpression)
                .putExpressionValue(":timeSlot", AttributeValue.builder().s(timeSlot).build())
                .putExpressionValue(":actionId", AttributeValue.builder().s(actionId).build())
                .build();

        FutureActionEntity updateEntity = getFutureActionEntity(timeSlot, actionId);
        return Mono.fromFuture(() -> table.updateItem(r -> r.item(updateEntity).conditionExpression(conditionExpressionUpdate).ignoreNulls(true)))
                .onErrorResume(ConditionalCheckFailedException.class, ex -> {
                    String message = String.format("Future action with actionId=%s and timeSlot=%s not found", actionId, timeSlot);
                    log.info(message, ex);
                    return Mono.error(new PnNotFoundException("Not found", message, ERROR_CODE_FUTURE_ACTION_NOTFOUND));
                }).then();
    }

    private FutureActionEntity getFutureActionEntity(String timeSlot, String actionId) {
        FutureActionEntity updateEntity = new FutureActionEntity();
        updateEntity.setTimeSlot(timeSlot);
        updateEntity.setLogicalDeleted(true);

        updateEntity.setActionId(actionId);
        return updateEntity;
    }
}
