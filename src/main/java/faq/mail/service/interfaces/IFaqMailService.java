package faq.mail.service.interfaces;

import javax.mail.Message;

public interface IFaqMailService {

    void processEmail(Message mailMessage);

}
