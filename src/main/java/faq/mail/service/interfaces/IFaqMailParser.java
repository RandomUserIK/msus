package faq.mail.service.interfaces;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.io.IOException;
import java.util.List;

public interface IFaqMailParser {

    String getTextFromMessage(Message message) throws MessagingException, IOException, javax.mail.MessagingException;

    String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException;

    List<File> getMultipart(Message message, String fileAttachmentPath) throws IOException, MessagingException;

}
