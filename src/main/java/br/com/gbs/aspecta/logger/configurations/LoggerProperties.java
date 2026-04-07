package br.com.gbs.aspecta.logger.configurations;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "logger")
@Validated
@Getter
@Setter
public class LoggerProperties {
    private boolean enabled = true;
    private boolean enableI18n = true;
    private String projectName;
    private List<String> sensitiveKeys = List.of(
            "password", "senha", "cpf", "cnpj", "token",
            "secret", "apikey", "accesstoken", "refreshtoken",
            "authorization", "privatekey", "creditcard"
    );

    /**
     * When false (default), the cause's message is never included in HTTP responses.
     * Enable only in non-production environments.
     */
    private boolean exposeExceptionDetails = false;

    /**
     * When true, log messages are emitted as a single JSON object instead of the
     * default human-readable template. Useful for log aggregators (Loki, OpenSearch, Datadog).
     */
    private boolean structuredOutput = false;

    @Valid
    private Async async = new Async();

    @Getter
    @Setter
    public static class Async {
        @Min(1) @Max(20)
        private int corePoolSize = 2;

        @Min(1) @Max(100)
        private int maxPoolSize = 8;

        @Min(1) @Max(5000)
        private int queueCapacity = 500;
    }
}
