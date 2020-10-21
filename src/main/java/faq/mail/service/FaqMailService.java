package faq.mail.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.integration.nae.service.NaeRestClient;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import faq.mail.domain.throwable.FaqMailProcessingException;
import faq.mail.models.EmailData;
import faq.mail.service.interfaces.IFaqMailParser;
import faq.mail.service.interfaces.IFaqMailService;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class FaqMailService implements IFaqMailService {

    @Value("${storage.path}")
    private String storagePath;

    @Autowired
    private IFaqMailParser faqMailParser;

    @Autowired
    private IDataPreparationService dataPreparationService;

    @Autowired
    private NaeRestClient naeRestClient;

    @Override
    public void processEmail(Message mailMessage) {
        EmailData emailData = new EmailData();

        try {
            emailData.setReceivedFrom((mailMessage.getFrom() == null || mailMessage.getFrom().length == 0)  ?
                            "" : ((InternetAddress) mailMessage.getFrom()[0]).getAddress());
            log.info("Processing the received e-mail from: " + emailData.getReceivedFrom());

            emailData.setMailBodyContent(faqMailParser.getTextFromMessage(mailMessage));
            emailData.setSubject(mailMessage.getSubject());
            emailData.setDateSent(DateFormatUtils.format(mailMessage.getSentDate(), "dd.MM.yyyy"));
            emailData.setDateReceived(DateFormatUtils.format(mailMessage.getReceivedDate(), "dd.MM.yyyy"));
            emailData.setAttachments(faqMailParser.getMultipart(mailMessage, storagePath + "/"));

            createEmailCaseInNae(emailData);
        } catch (MessagingException | IOException ex) {
            log.error("E-mail processing failed", ex);
            throw new FaqMailProcessingException("E-mail processing, which was received from: " + emailData.getReceivedFrom() + " failed");
        }
    }

    private void createEmailCaseInNae(EmailData emailData) {
        String faqCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("FAQ", "", naeRestClient.getNetDataHolder().getFaqPetriNetStringId())), false);
        String emailCaseId = dataPreparationService.extractStringId(naeRestClient.createCase(
                dataPreparationService.createCaseBody("E-mail", "", naeRestClient.getNetDataHolder().getEmailDataPetriNetStringId())), false);

        String faqNovaUlohaTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(faqCaseId, "4"), true);
        String emailTaskId = dataPreparationService.extractStringId(naeRestClient.findTaskByCaseAndTransition(emailCaseId, "2"), true);

        naeRestClient.assignTask(faqNovaUlohaTaskId);
        naeRestClient.assignTask(emailTaskId);

        Map<String, Map<String, Object>> dataSet = prepareEmailData(emailData, emailTaskId);

        naeRestClient.setData(emailTaskId, dataSet);

        try {
            if (!emailData.getAttachments().isEmpty())
                naeRestClient.saveFile(emailTaskId, "attachments",
                        zipAttachments(storagePath + "/" + emailTaskId + ".zip", emailData.getAttachments()).getFile());
        } catch (IOException ex) {
            log.error("Failed to put attachments into the dataSet", ex);
        }
    }

    private Map<String, Map<String, Object>> prepareEmailData(EmailData emailData, String emailTaskId) {
        Map<String, Map<String, Object>> dataSet = new LinkedHashMap<>();
        dataSet.put("faq_parent_mongo_id", dataPreparationService.makeDataSetEntry("text", emailData.getFaqParentMongoId()));
        dataSet.put("client_email", dataPreparationService.makeDataSetEntry("text", emailData.getReceivedFrom()));
        dataSet.put("subject", dataPreparationService.makeDataSetEntry("text", emailData.getSubject()));
        dataSet.put("content", dataPreparationService.makeDataSetEntry("text", emailData.getMailBodyContent()));
        dataSet.put("sent_date", dataPreparationService.makeDataSetEntry("text", emailData.getDateSent()));
        dataSet.put("received_date", dataPreparationService.makeDataSetEntry("text", emailData.getDateReceived()));
        return dataSet;
    }

    private ZipFile zipAttachments(String zipFilePath, List<File> attachments) throws ZipException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        zipFile.addFiles(attachments);
        return zipFile;
    }
}
