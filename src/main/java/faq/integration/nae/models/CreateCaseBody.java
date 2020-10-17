package faq.integration.nae.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCaseBody {

    private String title;

    private String color;

    private String netId;

}
