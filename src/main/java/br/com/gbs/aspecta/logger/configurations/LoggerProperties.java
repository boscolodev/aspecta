package br.com.gbs.aspecta.logger.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "logger")
@Getter
@Setter
public class LoggerProperties {
    private boolean enabled = true;
    private String projectName;
}
