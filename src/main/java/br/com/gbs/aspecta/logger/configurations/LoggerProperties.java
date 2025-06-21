package br.com.gbs.aspecta.logger.configurations;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "logger")
@Getter
@Setter
public class LoggerProperties {
    private boolean enabled = true;
    private boolean enableI18n = true;
    private String projectName;
    private List<String> sensitiveKeys = List.of("password", "senha", "cpf", "cnpj", "token");
}
