package it.pagopa.pn.actionmanager.dto;

import it.pagopa.pn.actionmanager.dto.action.ActionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionTypeIT {

    @Test
    void compareInternalAndGeneratedActionType() {
        Set<String> internalValues = Arrays.stream(ActionType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<String> openApiValues = Arrays.stream(it.pagopa.pn.actionmanager.generated.openapi.server.v1.dto.ActionType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        // Trova valori presenti solo nell'enum interno
        Set<String> onlyInInternal = internalValues.stream()
                .filter(value -> !openApiValues.contains(value))
                .collect(Collectors.toSet());

        // Trova valori presenti solo nell'enum OpenAPI
        Set<String> onlyInOpenApi = openApiValues.stream()
                .filter(value -> !internalValues.contains(value))
                .collect(Collectors.toSet());

        if (!onlyInInternal.isEmpty() || !onlyInOpenApi.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("Differenze trovate tra gli enum:\n");

            if (!onlyInInternal.isEmpty()) {
                errorMessage.append("Valori presenti solo nell'enum interno: ")
                        .append(onlyInInternal)
                        .append("\n");
            }

            if (!onlyInOpenApi.isEmpty()) {
                errorMessage.append("Valori presenti solo nell'enum OpenAPI: ")
                        .append(onlyInOpenApi)
                        .append("\n");
            }

            errorMessage.append("Enum interno: ").append(internalValues).append("\n");
            errorMessage.append("Enum OpenAPI: ").append(openApiValues);

            Assertions.fail(errorMessage.toString());
        }

        // Se arriviamo qui, gli enum sono identici
        System.out.println("Gli enum ActionType sono perfettamente allineati!");
        System.out.println("Valori comuni: " + internalValues);
    }
}
