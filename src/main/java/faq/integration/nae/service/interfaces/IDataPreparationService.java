package faq.integration.nae.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import faq.integration.nae.models.CreateCaseBody;

import java.util.Map;

public interface IDataPreparationService {

    Map<String, Object> makeDataSetEntry(String type, Object value);

    ObjectNode populateDataset(Map<String, Map<String, Object>> data);

    CreateCaseBody createCaseBody(String title, String color, String processIdentifier);

    String extractStringId(ObjectNode naeResponse, boolean isTaskSearch);
}
