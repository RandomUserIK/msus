package faq.integration.nae.models;

import lombok.Data;

@Data
public class CaseSearchRequestWrapper {

    private String naeCaseId;

    private String naeTransitionId;

    private String naeTaskId;
}
