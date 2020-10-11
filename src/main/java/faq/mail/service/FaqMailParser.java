package faq.mail.service;

import faq.mail.service.interfaces.IFaqMailParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FaqMailParser implements IFaqMailParser {

    @Override
    public String getTextFromMessage(Message message) throws MessagingException, IOException {
        String result = "";
        if (message.isMimeType("text/plain")) {
            result = message.getContent().toString();
        } else if (message.isMimeType("multipart/*")) {
            MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
            result = getTextFromMimeMultipart(mimeMultipart);
        }
        return result;
    }

    @Override
    public String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        for (int partIndex = 0; partIndex < mimeMultipart.getCount(); ++partIndex) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(partIndex);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break;
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent();
                result.append("\n").append(org.jsoup.Jsoup.parse(html).text());
            } else if (bodyPart.getContent() instanceof MimeMultipart)
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
        }
        return result.toString();
    }

    @Override
    public List<File> getMultipart(Message message, String fileAttachmentPath) throws IOException, MessagingException {
        Multipart multipart = (Multipart) message.getContent();
        List<File> attachments = new ArrayList<>();

        for (int partIndex = 0; partIndex < multipart.getCount(); ++partIndex) {
            BodyPart bodyPart = multipart.getBodyPart(partIndex);
            File attachment = new File(fileAttachmentPath + bodyPart.getFileName());

            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) && StringUtils.isBlank(bodyPart.getFileName())) {
                continue;
            }

            InputStream inputStream = bodyPart.getInputStream();
            try (FileOutputStream fileOutputStream = new FileOutputStream(attachment)) {
                byte[] buf = new byte[4096];
                int bytesRead;

                while ((bytesRead = inputStream.read(buf)) != -1) {
                    fileOutputStream.write(buf, 0, bytesRead);
                }
            }
            attachments.add(attachment);
        }
        return attachments;
    }
}
