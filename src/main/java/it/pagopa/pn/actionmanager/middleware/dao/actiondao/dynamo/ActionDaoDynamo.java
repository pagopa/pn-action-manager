package it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo;

import com.fasterxml.jackson.core.JsonProcessingException;
import it.pagopa.pn.actionmanager.config.PnActionManagerConfigs;
import it.pagopa.pn.actionmanager.dto.Action;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.ActionDao;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.mapper.DtoToEntityActionMapper;
import it.pagopa.pn.commons.abstractions.impl.MiddlewareTypes;
import it.pagopa.pn.commons.exceptions.PnInternalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;

import java.time.Duration;

import static it.pagopa.pn.commons.abstractions.impl.AbstractDynamoKeyValueStore.ATTRIBUTE_NOT_EXISTS;
import static it.pagopa.pn.commons.exceptions.PnExceptionsCodes.ERROR_CODE_PN_GENERIC_ERROR;

@Component
@Slf4j
@ConditionalOnProperty(name = ActionDao.IMPLEMENTATION_TYPE_PROPERTY_NAME, havingValue = MiddlewareTypes.DYNAMO)
public class ActionDaoDynamo implements ActionDao {
    private final DynamoDbAsyncTable<ActionEntity> table;
    private final Duration actionTtl;

    public ActionDaoDynamo(DynamoDbEnhancedAsyncClient dynamoDbEnhancedAsyncClient,
                           PnActionManagerConfigs pnActionManagerConfigs) {
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
                .item(DtoToEntityActionMapper.dtoToEntity(action, actionTtl))
                .conditionExpression(conditionExpressionPut)
                .build();

        return Mono.fromFuture(() -> table.putItem(request))
                .onErrorResume(ConditionalCheckFailedException.class, ex -> {
                    log.warn("Action already present in the table, actionId={} ", action.getActionId(), ex);
                    return Mono.empty();
                });
    }
}
