package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_FUTURE_ACTION_NOTFOUND;

@Component
@Slf4j
@ConditionalOnProperty(name = FutureActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME, havingValue = MiddlewareTypes.DYNAMO)
public class FutureActionDaoDynamo  implements FutureActionDao {
    private final DynamoDbTable<FutureActionEntity> dynamoDbTableFutureAction;

    protected FutureActionDaoDynamo(DynamoDbEnhancedClient dynamoDbEnhancedClient, PnActionManagerConfigs cfg) {
        this.dynamoDbTableFutureAction = dynamoDbEnhancedClient.table(cfg.getFutureActionDao().getTableName(), TableSchema.fromClass(FutureActionEntity.class));
    }

    @Override
    public void unscheduleAction(String timeSlot, String actionId) {
        String keyConditionExpression = String.format("%s = :timeSlot AND %s = :actionId",
                FutureActionEntity.FIELD_TIME_SLOT, FutureActionEntity.FIELD_ACTION_ID);

        Expression conditionExpressionUpdate = Expression.builder()
                .expression(keyConditionExpression)
                .putExpressionValue(":timeSlot", AttributeValue.builder().s(timeSlot).build())
                .putExpressionValue(":actionId", AttributeValue.builder().s(actionId).build())
                .build();

        FutureActionEntity updateEntity = new FutureActionEntity();
        updateEntity.setTimeSlot(timeSlot);
        updateEntity.setLogicalDeleted(true);
        updateEntity.setActionId(actionId);

        try {
            dynamoDbTableFutureAction.updateItem(r -> r.item(updateEntity).conditionExpression(conditionExpressionUpdate));
        } catch (ConditionalCheckFailedException ex) {
            String message = String.format("Action not found for timeSlot=%s and actionId=%s", timeSlot, actionId);
            log.error(message, ex);
            throw new PnNotFoundException("Not found", message, ERROR_CODE_FUTURE_ACTION_NOTFOUND);
        }
    }
}
