package it.pagopa.pn.actionmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PnNotFoundException extends PnRuntimeException {

    public PnNotFoundException(String message, String description, String errorcode) {
        super(message, description, HttpStatus.NOT_FOUND.value(), errorcode, null, description);
    }
}
