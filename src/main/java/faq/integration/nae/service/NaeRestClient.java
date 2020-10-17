package faq.integration.nae.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.configuration.NaeRestConfigurationProperties;
import faq.integration.nae.domain.throwable.InitPetriNetIdsException;
import faq.integration.nae.service.interfaces.INaeRestClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Slf4j
@Service
public class NaeRestClient implements INaeRestClient {

    @Autowired
    private NaeRestConfigurationProperties naeRestConfigurationProperties;

    @Autowired
    private RestTemplate restTemplate;

    private static final String IDENTIFIER_PROPERTY = "identifier";
    private static final String EMBEDDED_PROPERTY = "_embedded";
    private static final String PETRI_NET_REFERENCES_PROPERTY = "petriNetReferences";

    private HttpHeaders naeHttpHeaders;

    @Getter
    private String faqPetriNetId;

    @Getter
    private String emailDataPetriNetId;

    @Getter
    private String ticketDataPetriNetId;

    @PostConstruct
    private void initNaeAuthHeaders() {
        naeHttpHeaders = makeAuthHeaders();
        initPetriNetIds();
    }

    private void initPetriNetIds() {
        log.info("Initializing Petri Net ids");
        try {
            ObjectNode netNodes = getPetriNets();
            if (netNodes == null || netNodes.isEmpty() || netNodes.get(EMBEDDED_PROPERTY) == null ||
                    netNodes.get(EMBEDDED_PROPERTY).get(PETRI_NET_REFERENCES_PROPERTY) == null)
                throw new InitPetriNetIdsException("Petri Net ids initialized unsuccessfully");
            else
                extractNetIds(netNodes);
            log.info("Petri Net ids initialized successfully");
        } catch (RuntimeException ex) {
            log.error("Error while initializing Petri Net ids", ex);
            throw new InitPetriNetIdsException("Failed to init Petri net ids");
        }
    }

    private ObjectNode getPetriNets() {
        String url = naeRestConfigurationProperties.getHost() + naeRestConfigurationProperties.getPetrinetGetAllProcessesPath();

        HttpEntity entity = new HttpEntity<>(naeHttpHeaders);
        return restTemplate.exchange(url, HttpMethod.GET, entity, ObjectNode.class).getBody();
    }

    private void extractNetIds(ObjectNode netNodes) {
        netNodes.elements().forEachRemaining(netNode -> {
            if (netNode.get(PETRI_NET_REFERENCES_PROPERTY) == null)
                return;

            JsonNode netReferences = netNode.get(PETRI_NET_REFERENCES_PROPERTY);
            netReferences.elements().forEachRemaining(netReference -> {
                if (netReference.get(IDENTIFIER_PROPERTY) == null)
                    return;

                if (netReference.get(IDENTIFIER_PROPERTY).asText().contains("faq"))
                    faqPetriNetId = netReference.get(IDENTIFIER_PROPERTY).asText();
                else if (netReference.get(IDENTIFIER_PROPERTY).asText().contains("email_data"))
                    emailDataPetriNetId = netReference.get(IDENTIFIER_PROPERTY).asText();
                else if (netReference.get(IDENTIFIER_PROPERTY).asText().contains("ticket_data"))
                    ticketDataPetriNetId = netReference.get(IDENTIFIER_PROPERTY).asText();
            });
        });
    }

    private HttpHeaders makeAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(naeRestConfigurationProperties.getUsername(), naeRestConfigurationProperties.getPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}
