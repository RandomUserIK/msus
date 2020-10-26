package faq.ticket.web;

import faq.ticket.domain.throwable.AttachmentSubmissionException;
import faq.ticket.domain.throwable.TicketCreationException;
import faq.ticket.service.interfaces.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/faq")
public class TicketRestController {

    private static final String TICKET_CREATION_FAILURE = "Ticket creation was unsuccessful";
    private static final String ATTACHMENT_SUBMISSION_SUCCESS = "Attachment was submitted successfully";
    private static final String ATTACHMENT_SUBMISSION_FAILURE = "Attachment was submitted unsuccessfully";
    private static final String INVALID_REQUEST_HEADER = "Invalid request header";

    @Autowired
    private ITicketService ticketService;

    @PostMapping(value = "/ticket", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> faqApiTicketCreate(HttpServletRequest request,
                                                      @RequestBody Map<String, String> body) {
        if (isInvalidHeader(request)) {
            log.warn(INVALID_REQUEST_HEADER);
            return new ResponseEntity<>(TICKET_CREATION_FAILURE, HttpStatus.BAD_REQUEST);
        }

        String ticketCaseId = "";

        try {
            log.info("Creating new ticket");
            ticketCaseId = ticketService.resolveTicketSubmission(body);
        } catch (TicketCreationException ex) {
            log.error(TICKET_CREATION_FAILURE, ex);
            return new ResponseEntity<>(TICKET_CREATION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(ticketCaseId, HttpStatus.OK);
    }

    @PostMapping(value = "/ticket/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> apiBackboneAttachmentPost(HttpServletRequest request,
                                                            @PathVariable String caseId, MultipartFile file) {
        if (isInvalidHeader(request)) {
            log.warn(INVALID_REQUEST_HEADER);
            return new ResponseEntity<>(ATTACHMENT_SUBMISSION_FAILURE, HttpStatus.BAD_REQUEST);
        }

        try {
            ticketService.resolveAttachmentSubmission(caseId, file);
        } catch (AttachmentSubmissionException ex) {
            log.error(ATTACHMENT_SUBMISSION_FAILURE, ex);
            return new ResponseEntity<>(ATTACHMENT_SUBMISSION_FAILURE, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(ATTACHMENT_SUBMISSION_SUCCESS, HttpStatus.OK);
    }

    private boolean isInvalidHeader(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return (accept == null || !accept.contains("application/json"));
    }
}
