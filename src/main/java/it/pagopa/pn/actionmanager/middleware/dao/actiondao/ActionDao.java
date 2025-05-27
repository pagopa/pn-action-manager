package it.pagopa.pn.actionmanager.middleware.dao.actiondao;

import it.pagopa.pn.actionmanager.dto.action.Action;
import reactor.core.publisher.Mono;

public interface ActionDao {
    Mono<Void> addOnlyActionIfAbsent(Action action);
}
