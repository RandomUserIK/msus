package faq.mail.service;

import faq.mail.domain.throwable.FaqMailProcessingException;
import faq.mail.service.interfaces.IFaqMailParser;
import faq.mail.service.interfaces.IFaqMailService;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FaqMailService implements IFaqMailService {

    @Value("${storage.path}")
    private String storagePath;

    @Autowired
    private IFaqMailParser faqMailParser;

    @Override
    public void processEmail(Message mailMessage) {
        String mailBodyContent = "";
        String receivedFrom = "";
        List<File> attachments = new ArrayList<>();

        try {
            receivedFrom = (mailMessage.getFrom() == null || mailMessage.getFrom().length == 0)  ?
                            "" : ((InternetAddress) mailMessage.getFrom()[0]).getAddress();
            log.info("Processing the received e-mail from: " + receivedFrom);

            mailBodyContent = faqMailParser.getTextFromMessage(mailMessage);
            attachments = faqMailParser.getMultipart(mailMessage, storagePath + "/");

            // TODO: after content is extracted, send data to the Application Engine
        } catch (MessagingException | IOException ex) {
            log.error("E-mail processing failed", ex);
            throw new FaqMailProcessingException("E-mail processing, which was received from: " + receivedFrom + " failed");
        }
    }

    private ZipFile zipAttachments(String zipFilePath, List<File> attachments) throws ZipException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        zipFile.addFiles(attachments);
        return zipFile;
    }
}
