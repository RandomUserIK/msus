package faq.integration.nae.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
// data provides @AllArgsConstructor, however, without hardcoding this annotation,
// one cannot instantiate TaskSearchCaseRequest by calling all args constructor
// at least not in IntelliJ Idea...
@AllArgsConstructor
public class TaskSearchCaseRequest {

    private String id;
    
    private String title;

}
