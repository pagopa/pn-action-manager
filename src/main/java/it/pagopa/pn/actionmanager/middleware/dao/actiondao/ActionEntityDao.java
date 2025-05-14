package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.ActionEntity;
import it.pagopa.pn.commons.abstractions.KeyValueStore;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

public interface ActionEntityDao extends KeyValueStore<Key, ActionEntity> {

    TransactPutItemEnhancedRequest<ActionEntity> preparePutIfAbsent(ActionEntity action);
}
