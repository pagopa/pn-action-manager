package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.DtoToEntityActionMapper;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Duration;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTIONMANAGER_NOTFOUND;
import static it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore.ATTRIBUTE_NOT_EXISTS;
import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

@Component
@Slf4j
@ConditionalOnProperty(name = ActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME, havingValue = MiddlewareTypes.DYNAMO)
public class ActionDaoDynamo implements ActionDao {
    private final DynamoDbTable<ActionEntity> dynamoDbTableAction;
    private final DynamoDbTable<FutureActionEntity> dynamoDbTableFutureAction;
    private final Duration actionTtl;

    public ActionDaoDynamo(DynamoDbEnhancedClient dynamoDbEnhancedClient,
                           PnActionManagerConfigs pnActionManagerConfigs) {
        this.dynamoDbTableFutureAction = dynamoDbEnhancedClient.table(pnActionManagerConfigs.getFutureActionDao().getTableName(), TableSchema.fromClass(FutureActionEntity.class));
        this.dynamoDbTableAction = dynamoDbEnhancedClient.table(pnActionManagerConfigs.getActionDao().getTableName(), TableSchema.fromClass(ActionEntity.class));
        this.actionTtl = fromStringDaysToDuration(pnActionManagerConfigs.getActionTtlDays());
    }

    private static Duration fromStringDaysToDuration(String daysToFormat) {
        if(daysToFormat != null){
            long days = Long.parseLong(daysToFormat);
            return Duration.ofDays(days);
        }else {
            throw new PnInternalException("TTL for action cannot be null", ERROR_CODE_PN_GENERIC_ERROR);
        }
    }

    @Override
    public void addOnlyActionIfAbsent(Action action) {
        String expression = String.format(
                "%s(%s)",
                ATTRIBUTE_NOT_EXISTS,
                ActionEntity.FIELD_ACTION_ID
        );

        Expression conditionExpressionPut = Expression.builder()
                .expression(expression)
                .build();

        PutItemEnhancedRequest<ActionEntity> request = PutItemEnhancedRequest.builder( ActionEntity.class )
                .item(DtoToEntityActionMapper.dtoToEntity(action, actionTtl) )
                .conditionExpression( conditionExpressionPut )
                .build();
        try {
            dynamoDbTableAction.putItem(request);
        }catch (ConditionalCheckFailedException ex){
            log.warn("Exception code ConditionalCheckFailed is expected for retry, letting flow continue actionId={} ", action.getActionId(), ex);
        }
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
            throw new PnNotFoundException("Not found", message, ERROR_CODE_ACTIONMANAGER_NOTFOUND);
        }
    }
}
