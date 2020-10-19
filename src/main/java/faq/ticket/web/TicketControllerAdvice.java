package faq.ticket.web;

import faq.ticket.web.responsebodies.InternalServerErrorMessageResource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice(assignableTypes = TicketRestController.class)
public class TicketControllerAdvice {

    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    @ExceptionHandler
    public ResponseEntity<InternalServerErrorMessageResource> handleInternalServerException(Exception ex) {
        log.error("An error occurred while processing the request", ex);
        InternalServerErrorMessageResource internalServerError = new InternalServerErrorMessageResource();
        internalServerError.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        internalServerError.setError(INTERNAL_SERVER_ERROR);
        internalServerError.setTimestamp(System.currentTimeMillis());
        return new ResponseEntity<>(internalServerError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
