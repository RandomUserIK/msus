package faq.startup;

import faq.mail.service.interfaces.IFaqMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class FaqMailRunner implements CommandLineRunner {

    @Autowired
    private IFaqMailService faqMailService;

    @Autowired
    private ImapIdleChannelAdapter imapIdleChannelAdapter;

    @Override
    public void run(String... args) {
        if(imapIdleChannelAdapter == null || imapIdleChannelAdapter.getOutputChannel() == null)
            return;

        ((DirectChannel) imapIdleChannelAdapter.getOutputChannel()).subscribe(new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                javax.mail.Message mailMessage = (javax.mail.Message) message.getPayload();
                faqMailService.processEmail(mailMessage);
            }
        });
    }

}
