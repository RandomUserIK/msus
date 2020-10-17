package faq.integration.nae.models;

import lombok.Data;

import java.util.List;

@Data
public class TaskSearchRequestWrapper {

    private String naeCaseId;

    private List<String> naeTransitionId;

    private List<TaskSearchCaseRequest> useCase;

}
