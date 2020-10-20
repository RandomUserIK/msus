package faq.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.mail.ImapIdleChannelAdapter;
import org.springframework.integration.mail.ImapMailReceiver;

import java.util.Properties;

@EnableIntegration
@Configuration
public class IntegrationMailConfiguration {

    @Value("${faq.mail.imap.server.url}")
    private String serverUrl;

    @Value("${faq.mail.imap.server.secure}")
    private boolean isServerSecure;

    @Value("${faq.mail.imap.auth.plain}")
    private boolean plainAuth;

    @Value("${faq.mail.imap.auth.debug}")
    private boolean debugMode;

    @Bean
    public ImapMailReceiver imapMailReceiver() {
        ImapMailReceiver mailReceiver = new ImapMailReceiver(serverUrl);
        mailReceiver.setJavaMailProperties(getJavaMailProperties());
        mailReceiver.setShouldMarkMessagesAsRead(true);
        mailReceiver.setShouldDeleteMessages(true);
        mailReceiver.setSimpleContent(true);
        mailReceiver.setEmbeddedPartsAsBytes(true);
        return mailReceiver;
    }

    @Bean
    public ImapIdleChannelAdapter imapIdleChannelAdapter() {
        ImapIdleChannelAdapter channelAdapter = new ImapIdleChannelAdapter(imapMailReceiver());
        channelAdapter.setAutoStartup(true);
        channelAdapter.setOutputChannel(receiveDirectChannel());
        channelAdapter.setShouldReconnectAutomatically(true);
        return channelAdapter;
    }

    @Bean(name = "imapReceiveChannel")
    public DirectChannel receiveDirectChannel() {
        return new DirectChannel();
    }

    private Properties getJavaMailProperties() {
        Properties mailProperties = new Properties();
        mailProperties.setProperty("mail.imap.starttls.enable", "" + isServerSecure);
        mailProperties.setProperty("mail.imap.socketFactory.fallback", "false");
        mailProperties.setProperty("mail.imap.socketFactory.port", "993");
        mailProperties.setProperty("mail.store.protocol", "imap");
        mailProperties.setProperty("mail.imap.ssl.trust", "*");
        mailProperties.setProperty("mail.debug", "" + debugMode);

        if (!plainAuth)
            mailProperties.setProperty("mail.imap.auth.plain.disable", "true");

        return mailProperties;
    }
}
