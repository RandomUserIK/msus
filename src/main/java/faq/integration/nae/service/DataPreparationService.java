package faq.integration.nae.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.integration.nae.service.interfaces.IDataPreparationService;
import groovy.json.JsonOutput;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class DataPreparationService implements IDataPreparationService {

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

}
