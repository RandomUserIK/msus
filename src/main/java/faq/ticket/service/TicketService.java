package faq.ticket.service;

import faq.integration.nae.service.interfaces.INaeRestClient;
import faq.ticket.service.interfaces.ITicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class TicketService implements ITicketService {

    @Autowired
    private INaeRestClient naeRestClient;

    @Override
    public String resolveTicketSubmit(Map<String, String> body) {
        return null;
    }
}
