package it.pagopa.pn.actionmanager.rest;

import it.pagopa.pn.actionmanager.generated.openapi.server.v1.api.ActionApi;
import it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.NewAction;
import it.pagopa.pn.actionmanager.service.ActionService;
import it.pagopa.pn.actionmanager.service.mapper.SmartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ActionManagerController implements ActionApi {
    private final ActionService actionService;

    @Override
    public Mono<ResponseEntity<Void>> insertAction(Mono<NewAction> action, final ServerWebExchange exchange) {
        return action.flatMap(a -> actionService.addOnlyActionIfAbsent(SmartMapper.mapToClass(a, it.pagopa.pn.actionmanager.dto.action.Action.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.CREATED).build());
    }

    @Override
    public Mono<ResponseEntity<Void>> unscheduleAction(String actionId, String timeslot, final ServerWebExchange exchange) {
        return actionService.unscheduleAction(timeslot, actionId)
                .thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
}
