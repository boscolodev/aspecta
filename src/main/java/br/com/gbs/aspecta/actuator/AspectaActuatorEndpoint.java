package br.com.gbs.aspecta.actuator;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Spring Actuator endpoint that exposes the current Aspecta configuration and
 * real-time health of the async logging executor.
 * <p>
 * Accessible at {@code /actuator/aspecta} when {@code spring-boot-starter-actuator}
 * is on the classpath and the endpoint is enabled.
 * <p>
 * <strong>Security note:</strong> The list of sensitive-keys is intentionally
 * omitted to avoid leaking which fields the application considers sensitive.
 * Protect this endpoint with Spring Security before exposing it in production.
 */
@Endpoint(id = "aspecta")
@RequiredArgsConstructor
public class AspectaActuatorEndpoint {

    private final LoggerProperties loggerProperties;
    private final ThreadPoolTaskExecutor executor;

    @ReadOperation
    public Map<String, Object> info() {
        int    queueSize     = executor.getThreadPoolExecutor().getQueue().size();
        int    activeThreads = executor.getActiveCount();
        int    queueCapacity = loggerProperties.getAsync().getQueueCapacity();
        double usagePct      = queueCapacity > 0
                ? Math.round((double) queueSize / queueCapacity * 1000.0) / 10.0
                : 0.0;

        Map<String, Object> asyncStats = new LinkedHashMap<>();
        asyncStats.put("corePoolSize",   loggerProperties.getAsync().getCorePoolSize());
        asyncStats.put("maxPoolSize",    loggerProperties.getAsync().getMaxPoolSize());
        asyncStats.put("queueCapacity",  queueCapacity);
        asyncStats.put("activeThreads",  activeThreads);
        asyncStats.put("queueSize",      queueSize);
        asyncStats.put("queueUsagePct",  usagePct);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("enabled",          loggerProperties.isEnabled());
        result.put("projectName",      loggerProperties.getProjectName() != null
                                       ? loggerProperties.getProjectName() : "N/A");
        result.put("i18nEnabled",      loggerProperties.isEnableI18n());
        result.put("structuredOutput", loggerProperties.isStructuredOutput());
        result.put("async",            asyncStats);
        return result;
    }
}
