package faq.ticket.service;

import faq.integration.nae.service.NaeRestClient;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import faq.integration.nae.service.interfaces.INaeRestClient;
import faq.mail.models.EmailData;
import faq.ticket.domain.throwable.AttachmentSubmissionException;
import faq.ticket.service.interfaces.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
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
        String faqCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("FAQ", "", naeRestClient.getNetDataHolder().getFaqPetriNetStringId())), false);
        String ticketCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("Ticket", "", naeRestClient.getNetDataHolder().getTicketDataPetriNetStringId())), false);

        String faqNovaUlohaTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(faqCaseId, "4"), true);
        String ticketTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(ticketCaseId, "2"), true);

        naeRestClient.assignTask(faqNovaUlohaTaskId);
        naeRestClient.assignTask(ticketTaskId);
        naeRestClient.finishTask(ticketTaskId);

        ticketTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(ticketCaseId, "5"), true);

        Map<String, Map<String, Object>> dataSet = prepareTicketData(body, faqCaseId);
        naeRestClient.setData(ticketTaskId, dataSet);

        dataSet.clear();
        dataSet.put("channel", dataPreparationService.makeDataSetEntry("text", "e-Ticket"));
        dataSet.put("client_email", dataPreparationService.makeDataSetEntry("text", body.get("client_email")));
        dataSet.put("ticket_data", dataPreparationService.makeDataSetEntry("taskRef", Collections.singletonList(ticketTaskId)));
        naeRestClient.setData(faqNovaUlohaTaskId, dataSet);

        naeRestClient.assignTask(ticketTaskId);
        naeRestClient.finishTask(ticketTaskId);
        naeRestClient.cancelTask(faqNovaUlohaTaskId);

        return faqCaseId;
    }

    @Override
    public void resolveAttachmentSubmission(MultipartFile file) {
        try {
            File attachment = new File(storagePath + "/" + file.getOriginalFilename());
            attachment.createNewFile();
            OutputStream os = new FileOutputStream(attachment);
            os.write(file.getBytes());

            createDocumentAttachmentCase(attachment);
        } catch (IOException ex) {
            log.error(ATTACHMENT_SUBMISSION_FAILURE, ex);
            throw new AttachmentSubmissionException(ATTACHMENT_SUBMISSION_FAILURE);
        }
    }

    private Map<String, Map<String, Object>> prepareTicketData(Map<String, String> body, String faqParentId) {
        Map<String, Map<String, Object>> dataSet = new LinkedHashMap<>();
        dataSet.put("faq_parent_mongo_id", dataPreparationService.makeDataSetEntry("text", faqParentId));
        dataSet.put("client_email", dataPreparationService.makeDataSetEntry("text", body.get("client_email")));
        dataSet.put("client_name", dataPreparationService.makeDataSetEntry("text", body.get("client_name")));
        dataSet.put("client_surname", dataPreparationService.makeDataSetEntry("text", body.get("client_surname")));
        dataSet.put("ticket_id", dataPreparationService.makeDataSetEntry("text", body.get("ticketId")));
        dataSet.put("note", dataPreparationService.makeDataSetEntry("text", body.get("note")));
        dataSet.put("client_birth_number", dataPreparationService.makeDataSetEntry("text", body.get("client_birth_number")));
        dataSet.put("task_name", dataPreparationService.makeDataSetEntry("text", body.get("task_name")));
        return dataSet;
    }

    private void createDocumentAttachmentCase(File attachment) {
        String faqCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("FAQ", "", naeRestClient.getNetDataHolder().getFaqPetriNetStringId())), false);
        String attachmentCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("Attachment", "", naeRestClient.getNetDataHolder().getDocumentPetriNetStringId())), false);

        String faqNovaUlohaTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(faqCaseId, "4"), true);
        String attachmentTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(attachmentCaseId, "2"), true);

        naeRestClient.assignTask(faqNovaUlohaTaskId);
        naeRestClient.assignTask(attachmentTaskId);
        naeRestClient.finishTask(attachmentTaskId);

        attachmentTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(attachmentCaseId, "5"), true);

        Map<String, Map<String, Object>> dataSet = prepareAttachmentData(faqCaseId, attachment.getName());
        naeRestClient.setData(attachmentTaskId, dataSet);

        try {
            naeRestClient.saveFile(attachmentTaskId, "document", attachment);
        } catch (RuntimeException ex) {
            log.error("Failed to send attachment to NAE", ex);
            throw new AttachmentSubmissionException(ATTACHMENT_SUBMISSION_FAILURE);
        }

        dataSet.clear();
        dataSet.put("channel", dataPreparationService.makeDataSetEntry("text", "Attachment upload"));
        dataSet.put("document_data", dataPreparationService.makeDataSetEntry("taskRef", Collections.singletonList(attachmentTaskId)));
        naeRestClient.setData(faqNovaUlohaTaskId, dataSet);

        naeRestClient.assignTask(attachmentTaskId);
        naeRestClient.finishTask(attachmentTaskId);
        naeRestClient.cancelTask(faqNovaUlohaTaskId);
    }

    private Map<String, Map<String, Object>> prepareAttachmentData(String faqParentId, String filename) {
        Map<String, Map<String, Object>> dataSet = new LinkedHashMap<>();
        dataSet.put("faq_parent_mongo_id", dataPreparationService.makeDataSetEntry("text", faqParentId));
        dataSet.put("doc_number", dataPreparationService.makeDataSetEntry("text", "12345678"));
        dataSet.put("received_date", dataPreparationService.makeDataSetEntry("text", DateFormatUtils.format(new Date(), "dd.MM.yyyy")));
        dataSet.put("doc_type", dataPreparationService.makeDataSetEntry("text", "Vyžiadaná príloha"));
        dataSet.put("filename", dataPreparationService.makeDataSetEntry("text", filename));
        dataSet.put("note", dataPreparationService.makeDataSetEntry("text", "Poznámka pre riešiteľa úlohy. V prílohe zasielam vyžiadaný dokument."));
        return dataSet;
    }
}
