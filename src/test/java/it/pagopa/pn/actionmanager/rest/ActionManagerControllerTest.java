package it.pagopa.pn.actionmanager.rest;

import it.pagopa.pn.actionmanager.service.ActionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ActionManagerControllerTest {

    @Mock
    private ActionService actionService;

    @InjectMocks
    private ActionManagerController actionManagerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testInsertAction() {

        it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.NewAction inputAction =
                new it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.NewAction();

        when(actionService.addOnlyActionIfAbsent(any())).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> response = actionManagerController.insertAction(Mono.just(inputAction), mock(ServerWebExchange.class));

        StepVerifier.create(response)
                .expectNext(ResponseEntity.status(201).build())
                .verifyComplete();
        verify(actionService, times(1)).addOnlyActionIfAbsent(Mockito.any());
    }

    @Test
    void testUnscheduleAction() {
        String actionId = "testActionId";
        String timeslot = "testtimeslot";

        when(actionService.unscheduleAction(anyString(), anyString())).thenReturn(Mono.empty());

        Mono<ResponseEntity<Void>> response = actionManagerController.unscheduleAction(actionId, timeslot, mock(ServerWebExchange.class));

        assertEquals(ResponseEntity.status(204).build(), response.block());
        verify(actionService, times(1)).unscheduleAction(timeslot, actionId);
    }
}