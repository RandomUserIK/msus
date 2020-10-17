package faq.integration.nae.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TaskSearchRequestWrapper {

    @JsonProperty("case")
    public List<TaskSearchCaseRequest> useCase;

    public List<String> title;

    public List<Long> user;

    public List<String> process;

    public List<String> transitionId;

    public String fullText;

    public List<String> group;
}
