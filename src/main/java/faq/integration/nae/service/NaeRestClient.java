package faq.integration.nae.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.configuration.NaeRestConfigurationProperties;
import faq.integration.nae.domain.throwable.InitPetriNetIdsException;
import faq.integration.nae.models.CreateCaseBody;
import faq.integration.nae.models.TaskSearchCaseRequest;
import faq.integration.nae.models.TaskSearchRequestWrapper;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import faq.integration.nae.service.interfaces.INaeRestClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Service
public class NaeRestClient implements INaeRestClient {

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

    @Autowired
    private NaeRestConfigurationProperties naeRestConfigurationProperties;

    @Autowired
    private IDataPreparationService dataPreparationService;

    @Autowired
    private RestTemplate restTemplate;

    @PostConstruct
    private void initNaeAuthHeaders() {
        naeHttpHeaders = makeAuthHeaders();
        initPetriNetIds();
    }

    @Override
    public ObjectNode findTaskByCaseAndTransition(String naeCaseId, String naeTransId) {
        TaskSearchRequestWrapper taskSearchRequest = new TaskSearchRequestWrapper();
        taskSearchRequest.setUseCase(Collections.singletonList(new TaskSearchCaseRequest(naeCaseId, null)));
        taskSearchRequest.setNaeTransitionId(Collections.singletonList(naeTransId));
        return searchTasks(taskSearchRequest);
    }

    @Override
    public ObjectNode createCase(CreateCaseBody body) {
        String url = naeRestConfigurationProperties.getHost() + naeRestConfigurationProperties.getCreateCasePath();
        HttpEntity<CreateCaseBody> entity = new HttpEntity<>(body, naeHttpHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, ObjectNode.class);
        return response.getBody();
    }

    @Override
    public ObjectNode assignTask(String taskId) {
        String url = naeRestConfigurationProperties.getHost() +
                     naeRestConfigurationProperties.getAssignTaskPath().replace("{id}", taskId);
        HttpEntity entity = new HttpEntity<>(naeHttpHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, ObjectNode.class);
        return response.getBody();
    }

    @Override
    public ObjectNode finishTask(String taskId) {
        String url = naeRestConfigurationProperties.getHost() +
                     naeRestConfigurationProperties.getFinishTaskPath().replace("{id}", taskId);
        HttpEntity entity = new HttpEntity<>(naeHttpHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, ObjectNode.class);
        return response.getBody();
    }

    @Override
    public ObjectNode setData(String taskId, Map<String, Map<String, Object>> data) {
        String url = naeRestConfigurationProperties.getHost() +
                     naeRestConfigurationProperties.getTaskSetDataPath().replace("{id}", taskId);
        ObjectNode node = dataPreparationService.populateDataset(data);
        HttpEntity entity = new HttpEntity<>(node, naeHttpHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, ObjectNode.class);
        return response.getBody();
    }

    @Override
    public ObjectNode saveFile(String taskId, String fieldId, File file) {
        String url = naeRestConfigurationProperties.getHost() +
                     naeRestConfigurationProperties.getTaskSaveFilePath().replace("{id}", taskId)
                                                                         .replace("{fieldId}", fieldId);
        HttpHeaders multipartHeaders = makeAuthHeaders();
        multipartHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestWrapper = new LinkedMultiValueMap<>();
        requestWrapper.add("file", new FileSystemResource(file));
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(requestWrapper, multipartHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, ObjectNode.class);
        return response.getBody();
    }

    private ObjectNode searchTasks(TaskSearchRequestWrapper searchCriteria) {
        String url = naeRestConfigurationProperties.getHost() + naeRestConfigurationProperties.getSearchTaskPath();
        HttpEntity<TaskSearchRequestWrapper> entity = new HttpEntity<>(searchCriteria, naeHttpHeaders);
        ResponseEntity<ObjectNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, ObjectNode.class);
        return response.getBody();
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
