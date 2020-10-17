package faq.integration.nae.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.integration.nae.models.CreateCaseBody;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.util.Map;

public interface INaeRestClient {

    ObjectNode findTaskByCaseAndTransition(String caseId, String transId);

    ObjectNode createCase(CreateCaseBody body);

    ObjectNode assignTask(String taskId);

    ObjectNode finishTask(String taskId);

    ObjectNode setData(String taskId, Map<String, Map<String, Object>> data);

    ObjectNode saveFile(String taskId, String fieldId, File file);

}
