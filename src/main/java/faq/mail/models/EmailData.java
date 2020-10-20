package faq.mail.models;

import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Data
public class EmailData {

    private String faqParentMongoId;

    private String mailBodyContent;

    private String subject;

    private String dateSent;

    private String dateReceived;

    private String receivedFrom;

    private List<File> attachments;

    public EmailData() {
        attachments = new ArrayList<>();
    }
}
