package br.com.gbs.aspecta.logger.configurations;

import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

/**
 * Configures a dedicated async executor for Aspecta logging.
 * <p>
 * Uses a <strong>named</strong> bean ({@code aspectaLoggerExecutor}) so that only
 * Aspecta's {@code @Async} methods are affected — the consumer application's
 * own async configuration is left completely untouched.
 * <p>
 * A {@code TaskDecorator} propagates the MDC context (traceId, spanId, etc.)
 * from the calling thread to each worker thread, preserving full request traceability.
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncLoggerConfig {

    private final LoggerProperties loggerProperties;

    @Bean(name = "aspectaLoggerExecutor")
    public ThreadPoolTaskExecutor aspectaLoggerExecutor() {
        LoggerProperties.Async cfg = loggerProperties.getAsync();
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(cfg.getCorePoolSize());
        executor.setMaxPoolSize(cfg.getMaxPoolSize());
        executor.setQueueCapacity(cfg.getQueueCapacity());
        executor.setThreadNamePrefix("aspecta-logger-");
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        // Propagate MDC context from calling thread to async worker thread
        executor.setTaskDecorator(runnable -> {
            Map<String, String> mdcCopy = MDC.getCopyOfContextMap();
            return () -> {
                try {
                    if (mdcCopy != null) MDC.setContextMap(mdcCopy);
                    else MDC.clear();
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });

        executor.initialize();
        return executor;
    }
}
