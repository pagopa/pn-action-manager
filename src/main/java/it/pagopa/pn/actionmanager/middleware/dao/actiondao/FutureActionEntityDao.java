package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import it.pagopa.pn.actionmanager.middleware.dao.actiondao.dynamo.entity.FutureActionEntity;
import it.pagopa.pn.commons.abstractions.KeyValueStore;

import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactPutItemEnhancedRequest;

import java.util.Set;

public interface FutureActionEntityDao extends KeyValueStore<Key, FutureActionEntity> {
    Set<FutureActionEntity> findByTimeSlot(String timeSlot);

    TransactPutItemEnhancedRequest<FutureActionEntity> preparePut(FutureActionEntity action);
}
