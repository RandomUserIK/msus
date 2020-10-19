package faq.ticket.web;

import faq.ticket.domain.throwable.TicketCreationException;
import faq.ticket.service.interfaces.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/faq")
public class TicketRestController {

    private static final String TICKET_CREATION_FAILURE = "Ticket creation was unsuccessful";

    @Autowired
    private ITicketService ticketService;

    @PostMapping(value = "/ticket", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> faqApiTicketCreate(HttpServletRequest request,
                                                      @RequestBody Map<String, String> body) {
        if (isInvalidHeader(request)) {
            log.warn("Invalid request header");
            return new ResponseEntity<>(TICKET_CREATION_FAILURE, HttpStatus.NOT_IMPLEMENTED);
        }

        String ticketCaseId = "";

        try {
            log.info("Creating new ticket");
            ticketCaseId = ticketService.resolveTicketSubmit(body);
        } catch (TicketCreationException ex) {
            log.error(TICKET_CREATION_FAILURE, ex);
            return new ResponseEntity<>(TICKET_CREATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(ticketCaseId, HttpStatus.OK);
    }

    private boolean isInvalidHeader(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept == null || !accept.contains("application/json"));
    }
}
