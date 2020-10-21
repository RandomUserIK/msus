package faq.ticket.service.interfaces;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface ITicketService {

    String resolveTicketSubmission(Map<String, String> body);

    void resolveAttachmentSubmission(String caseId, MultipartFile file);
}
