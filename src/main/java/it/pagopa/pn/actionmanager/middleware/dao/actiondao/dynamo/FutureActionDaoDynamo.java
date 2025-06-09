package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.FutureActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.ActionDetailsConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Instant;
import java.util.Map;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_FUTURE_ACTION_NOTFOUND;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primarySortKey;

@Component
@Slf4j
public class FutureActionDaoDynamo  implements FutureActionDao {
    private final DynamoDbAsyncTable<FutureActionEntity> table;

    protected FutureActionDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient, PnActionManagerConfigs cfg) {
        this.table = initializeTable(cfg.getFutureActionDao().getTableName(), dynamoDbEnhancedAsyncClient);
    }

    private DynamoDbAsyncTable<FutureActionEntity> initializeTable(String tableName, DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient) {
        StaticTableSchema<FutureActionEntity> schemaTable = StaticTableSchema.builder(FutureActionEntity.class)
                .newItemSupplier(FutureActionEntity::new)
                .addAttribute(String.class, a -> a.name(FutureActionEntity.FIELD_TIME_SLOT)
                        .getter(FutureActionEntity::getTimeSlot)
                        .setter(FutureActionEntity::setTimeSlot)
                        .tags(primaryPartitionKey())
                )
                .addAttribute(String.class, a -> a.name(FutureActionEntity.FIELD_ACTION_ID)
                        .getter(FutureActionEntity::getActionId)
                        .setter(FutureActionEntity::setActionId)
                        .tags(primarySortKey())
                )
                .addAttribute(String.class, a -> a.name(FutureActionEntity.FIELD_IUN)
                        .getter(FutureActionEntity::getIun)
                        .setter(FutureActionEntity::setIun)
                )
                .addAttribute(Instant.class, a -> a.name(FutureActionEntity.FIELD_NOT_BEFORE)
                        .getter(FutureActionEntity::getNotBefore)
                        .setter(FutureActionEntity::setNotBefore)
                )
                .addAttribute(Integer.class, a -> a.name(FutureActionEntity.FIELD_RECIPIENT_INDEX)
                        .getter(FutureActionEntity::getRecipientIndex)
                        .setter(FutureActionEntity::setRecipientIndex)
                )
                .addAttribute(Boolean.class, a -> a.name(FutureActionEntity.FIELD_LOGICAL_DELETED)
                        .getter(FutureActionEntity::getLogicalDeleted)
                        .setter(FutureActionEntity::setLogicalDeleted)
                )
                .addAttribute(String.class, a -> a.name(FutureActionEntity.FIELD_TIMELINE_ID)
                        .getter(FutureActionEntity::getTimelineId)
                        .setter(FutureActionEntity::setTimelineId)
                )
                .addAttribute(Map.class, a -> a.name(FutureActionEntity.FIELD_DETAILS)
                        .getter(FutureActionEntity::getDetails)
                        .setter(FutureActionEntity::setDetails)
                        .attributeConverter((AttributeConverter) new ActionDetailsConverter())
                ).build();

        return dynamoDbEnhancedAsyncClient.table(tableName, schemaTable);
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
