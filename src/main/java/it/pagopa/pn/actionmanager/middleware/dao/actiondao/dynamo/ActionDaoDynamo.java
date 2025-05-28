package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.dto.action.ActionType;
import it.pagopa.pn.actionmanager.exceptions.PnConflictException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.DtoToEntityActionMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.enhanced.dynamodb.mapper.StaticTableSchema;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTION_CONFLICT;
import static it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore.ATTRIBUTE_NOT_EXISTS;
import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;
import static software.amazon.awssdk.enhanced.dynamodb.mapper.StaticAttributeTags.primaryPartitionKey;

@Component
@Slf4j
public class ActionDaoDynamo implements ActionDao {
    private final DynamoDbAsyncTable<ActionEntity> table;
    private final Duration actionTtl;
    private final DtoToEntityActionMapper dtoToEntityActionMapper;

    public ActionDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                           PnActionManagerConfigs pnActionManagerConfigs, DtoToEntityActionMapper dtoToEntityActionMapper) {
        this.dtoToEntityActionMapper = dtoToEntityActionMapper;
        //this.table = dynamoDbEnhancedAsyncClient.table(pnActionManagerConfigs.getActionDao().getTableName(), TableSchema.fromClass(ActionEntity.class));

        //todo add addAttribute for staticTableSchema
        StaticTableSchema<ActionEntity> schemaTable = StaticTableSchema.builder(ActionEntity.class)
                .newItemSupplier(ActionEntity::new)
                .addAttribute(String.class, a -> a.name(ActionEntity.FIELD_ACTION_ID)
                        .getter(ActionEntity::getActionId)
                        .setter(ActionEntity::setActionId)
                        .tags(primaryPartitionKey())
                )
                .addAttribute(String.class, a -> a.name(ActionEntity.FIELD_IUN)
                        .getter(ActionEntity::getIun)
                        .setter(ActionEntity::setIun)
                )
                .addAttribute(Instant.class, a -> a.name(ActionEntity.FIELD_NOT_BEFORE)
                        .getter(ActionEntity::getNotBefore)
                        .setter(ActionEntity::setNotBefore)
                )
                .addAttribute(Integer.class, a -> a.name(ActionEntity.FIELD_RECIPIENT_INDEX)
                        .getter(ActionEntity::getRecipientIndex)
                        .setter(ActionEntity::setRecipientIndex)
                )
                .addAttribute(String.class, a -> a.name(ActionEntity.FIELD_TIMESLOT)
                        .getter(ActionEntity::getTimeslot)
                        .setter(ActionEntity::setTimeslot)
                )
                .addAttribute(String.class, a -> a.name(ActionEntity.FIELD_TIMELINE_ID)
                        .getter(ActionEntity::getTimelineId)
                        .setter(ActionEntity::setTimelineId)
                )
                .addAttribute(Long.class, a -> a.name(ActionEntity.FIELD_TTL)
                        .getter(ActionEntity::getTtl)
                        .setter(ActionEntity::setTtl)
                )
                /*
                .addAttribute(Map.class, a -> a.name(ActionEntity.FIELD_DETAILS)
                        .getter(ActionEntity::getDetails)
                        .setter(ActionEntity::setDetails)
                )
                .addAttribute(ActionType.class, a -> a.name(ActionEntity.FIELD_TYPE)
                        .getter(ActionEntity::getType)
                        .setter(ActionEntity::setType)
                )*/
                .build();


        this.table = dynamoDbEnhancedAsyncClient.table(pnActionManagerConfigs.getActionDao().getTableName(), schemaTable);


        this.actionTtl = fromStringDaysToDuration(pnActionManagerConfigs.getActionTtlDays());
    }

    private static Duration fromStringDaysToDuration(String daysToFormat) {
        if (daysToFormat != null) {
            long days = Long.parseLong(daysToFormat);
            return Duration.ofDays(days);
        } else {
            throw new PnInternalException("TTL for action cannot be null", ERROR_CODE_PN_GENERIC_ERROR);
        }
    }

    @Override
    public Mono<Void> addOnlyActionIfAbsent(Action action) {
        String expression = String.format(
                "%s(%s)",
                ATTRIBUTE_NOT_EXISTS,
                ActionEntity.FIELD_ACTION_ID
        );

        Expression conditionExpressionPut = Expression.builder()
                .expression(expression)
                .build();

        PutItemEnhancedRequest<ActionEntity> request = PutItemEnhancedRequest.builder(ActionEntity.class)
                .item(dtoToEntityActionMapper.dtoToEntity(action, actionTtl))
                .conditionExpression(conditionExpressionPut)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .onErrorResume(ConditionalCheckFailedException.class, ex -> {
                    String message = String.format("Action already present in the table, actionId=%s ", action.getActionId());
                    log.info(message, ex);
                    return Mono.error(new PnConflictException("Conflict", message, ERROR_CODE_ACTION_CONFLICT));
                }).then();
    }
}
