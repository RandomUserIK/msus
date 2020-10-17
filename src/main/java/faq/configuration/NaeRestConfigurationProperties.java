package faq.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nae")
public class NaeRestConfigurationProperties {

    private String host;

    private String username;

    private String password;

    private String createCasePath;

    private String getTasksPath;

    private String assignTaskPath;

    private String finishTaskPath;

    private String taskSaveFilePath;

    private String taskSetDataPath;

    private String petrinetGetAllProcessesPath;

}
