package faq.integration.nae.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.integration.nae.models.CreateCaseBody;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import groovy.json.JsonOutput;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DataPreparationService implements IDataPreparationService {

    private static final String STRING_ID = "stringId";

    @Override
    public Map<String, Object> makeDataSetEntry(String type, Object value) {
        Map<String, Object> fieldMap = new HashMap<>();
        fieldMap.put("type", type);
        fieldMap.put("value", value);
        return fieldMap;
    }

    @Override
    public ObjectNode populateDataset(Map<String, Map<String, Object>> data) {
        ObjectMapper mapper = new ObjectMapper();
        String json = JsonOutput.toJson(data);
        try {
            return (ObjectNode) mapper.readTree(json);
        } catch (IOException e) {
            log.error("Dataset cannot be populated. " + e.getMessage(), e);
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Override
    public CreateCaseBody createCaseBody(String title, String color, String processIdentifier) {
        return new CreateCaseBody(title, color, processIdentifier);
    }

    @Override
    public String extractStringId(ObjectNode naeResponse, boolean isTaskSearch) {
        return isTaskSearch ? naeResponse.get("_embedded").get("tasks").get(0).get(STRING_ID).asText() : naeResponse.get(STRING_ID).asText();
    }

    @Override
    public ZipFile zipAttachments(String zipFilePath, List<File> attachments) throws ZipException {
        ZipFile zipFile = new ZipFile(zipFilePath);
        zipFile.addFiles(attachments);
        return zipFile;
    }
}
