package br.com.gbs.aspecta.actuator;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConditionalOnClass(Endpoint.class)
public class AspectaActuatorConfig {

    @Bean
    @ConditionalOnMissingBean
    public AspectaActuatorEndpoint aspectaActuatorEndpoint(
            LoggerProperties loggerProperties,
            @Qualifier("aspectaLoggerExecutor") ThreadPoolTaskExecutor executor) {
        return new AspectaActuatorEndpoint(loggerProperties, executor);
    }
}
