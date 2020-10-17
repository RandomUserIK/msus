package faq.integration.nae.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

public interface IDataPreparationService {

    Map<String, Object> makeDataSetEntry(String type, Object value);

    ObjectNode populateDataset(Map<String, Map<String, Object>> data);

}
