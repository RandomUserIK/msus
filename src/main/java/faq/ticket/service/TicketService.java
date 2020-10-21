package faq.ticket.service;

import faq.integration.nae.service.NaeRestClient;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import faq.integration.nae.service.interfaces.INaeRestClient;
import faq.ticket.domain.throwable.AttachmentSubmissionException;
import faq.ticket.service.interfaces.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Map;

@Slf4j
@Service
public class TicketService implements ITicketService {

    private static final String ATTACHMENT_SUBMISSION_FAILURE = "Attachment was submitted unsuccessfully";

    @Value("${storage.path}")
    private String storagePath;

    @Autowired
    private NaeRestClient naeRestClient;

    @Autowired
    private IDataPreparationService dataPreparationService;

    @Override
    public String resolveTicketSubmission(Map<String, String> body) {
        return null;
    }

    @Override
    public void resolveAttachmentSubmission(String caseId, MultipartFile file) {
        try {
            File attachment = new File(storagePath + "/" + file.getOriginalFilename());
            attachment.createNewFile();
            OutputStream os = new FileOutputStream(attachment);
            os.write(file.getBytes());

            String fileType = Files.probeContentType(attachment.toPath());

            if (fileType.equals("PDF") || fileType.equals("DOCX"))
                createDocumentAttachmentCase(caseId, attachment);
            else {
                log.error(ATTACHMENT_SUBMISSION_FAILURE);
                throw new AttachmentSubmissionException(ATTACHMENT_SUBMISSION_FAILURE);
            }
        } catch (IOException ex) {
            log.error(ATTACHMENT_SUBMISSION_FAILURE, ex);
            throw new AttachmentSubmissionException(ATTACHMENT_SUBMISSION_FAILURE);
        }
    }

    private void createDocumentAttachmentCase(String caseId, File attachment) {
        // TODO:
//        String attachmentCaseId = naeRestClient.createCase(
//                dataPreparationService.createCaseBody("FAQ Attachment", "", naeRestClient.getNetDataHolder().getFaqPetriNetStringId()));

//        if (attachmentCaseId == null || attachmentCaseId.isEmpty()) {
//            log.error(ATTACHMENT_SUBMISSION_FAILURE);
//            throw new AttachmentSubmissionException(ATTACHMENT_SUBMISSION_FAILURE);
//        }
    }
}
