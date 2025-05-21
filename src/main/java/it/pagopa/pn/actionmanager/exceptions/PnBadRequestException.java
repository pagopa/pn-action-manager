package it.pagopa.pn.actionmanager.exceptions;

import it.pagopa.pn.commons.exceptions.PnRuntimeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PnBadRequestException extends PnRuntimeException {
    public PnBadRequestException(String message, String description, String errorcode) {
        super(message, description, HttpStatus.BAD_REQUEST.value(), errorcode, null, null);
    }

    public PnBadRequestException(String message, String description, String errorcode, Throwable ex) {
        super(message, description, HttpStatus.BAD_REQUEST.value(), errorcode, null, null, ex);
    }
}
