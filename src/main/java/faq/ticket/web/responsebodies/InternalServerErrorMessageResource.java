package faq.ticket.web.responsebodies;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InternalServerErrorMessageResource {

    @JsonProperty("timestamp")
    private Long timestamp = null;

    @JsonProperty("status")
    private Integer status = null;

    @JsonProperty("error")
    private String error = null;

}
