package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.action.Action;
import it.pagopa.pn.actionmanager.exceptions.PnConflictException;
import it.pagopa.pn.actionmanager.exceptions.PnNotFoundException;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.DtoToEntityActionMapper;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.EntityToDtoActionMapper;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Duration;

import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTION_CONFLICT;
import static it.pagopa.pn.actionmanager.exceptions.PnActionManagerExceptionCodes.ERROR_CODE_ACTION_NOT_FOUND;
import static it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore.ATTRIBUTE_NOT_EXISTS;
import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

@Component
@Slf4j
public class ActionDaoDynamo implements ActionDao {
    private final DynamoDbAsyncTable<ActionEntity> table;
    private final Duration actionTtl;
    private final DtoToEntityActionMapper dtoToEntityActionMapper;
    private final EntityToDtoActionMapper entityToDtoActionMapper;

    public ActionDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                           PnActionManagerConfigs pnActionManagerConfigs, DtoToEntityActionMapper dtoToEntityActionMapper, EntityToDtoActionMapper entityToDtoActionMapper) {
        this.dtoToEntityActionMapper = dtoToEntityActionMapper;
        this.entityToDtoActionMapper = entityToDtoActionMapper;
        this.table = dynamoDbEnhancedAsyncClient.table(pnActionManagerConfigs.getActionDao().getTableName(), TableSchema.fromClass(ActionEntity.class));
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

    @Override
    public Mono<Action> getAction(String actionId) {
        return Mono.fromFuture(() -> table.getItem(Key.builder().partitionValue(actionId).build()))
                .map(entityToDtoActionMapper::entityToDto)
                .switchIfEmpty(Mono.error(new PnNotFoundException("Not found", "Action not found with id: " + actionId, ERROR_CODE_ACTION_NOT_FOUND)));
    }
}
