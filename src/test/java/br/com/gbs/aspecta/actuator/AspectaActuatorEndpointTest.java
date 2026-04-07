package br.com.gbs.aspecta.actuator;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AspectaActuatorEndpoint")
class AspectaActuatorEndpointTest {

    @Mock private LoggerProperties props;
    @Mock private LoggerProperties.Async asyncCfg;
    @Mock private ThreadPoolTaskExecutor executor;
    @Mock private ThreadPoolExecutor threadPoolExecutor;

    private AspectaActuatorEndpoint endpoint;

    @BeforeEach
    void setUp() {
        when(props.getAsync()).thenReturn(asyncCfg);
        when(asyncCfg.getCorePoolSize()).thenReturn(2);
        when(asyncCfg.getMaxPoolSize()).thenReturn(8);
        when(asyncCfg.getQueueCapacity()).thenReturn(500);
        when(props.isEnabled()).thenReturn(true);
        when(props.getProjectName()).thenReturn("TEST");
        when(props.isEnableI18n()).thenReturn(true);
        when(props.isStructuredOutput()).thenReturn(false);
        when(executor.getThreadPoolExecutor()).thenReturn(threadPoolExecutor);
        when(threadPoolExecutor.getQueue()).thenReturn(new LinkedBlockingQueue<>());
        when(executor.getActiveCount()).thenReturn(0);
        endpoint = new AspectaActuatorEndpoint(props, executor);
    }

    @Test
    @DisplayName("Quando info chamado deve conter os campos esperados de nível raiz")
    void whenInfoCalledShouldContainExpectedTopLevelFields() {
        Map<String, Object> info = endpoint.info();
        assertThat(info).containsKeys("enabled", "projectName", "i18nEnabled", "structuredOutput", "async");
    }

    @Test
    @DisplayName("Quando enabled é verdadeiro deve refletir flag no resultado")
    void whenEnabledTrueShouldReflectFlagInResult() {
        assertThat(endpoint.info()).containsEntry("enabled", true);
    }

    @Test
    @DisplayName("Quando projectName configurado deve aparecer no resultado")
    void whenProjectNameConfiguredShouldAppearInResult() {
        assertThat(endpoint.info()).containsEntry("projectName", "TEST");
    }

    @Test
    @DisplayName("Quando projectName é nulo deve usar N/A como fallback")
    void whenProjectNameIsNullShouldFallBackToNA() {
        when(props.getProjectName()).thenReturn(null);
        assertThat(endpoint.info()).containsEntry("projectName", "N/A");
    }

    @Test
    @DisplayName("Quando async stats solicitado deve conter campos de pool e fila")
    void whenAsyncStatsRequestedShouldContainPoolAndQueueFields() {
        @SuppressWarnings("unchecked")
        Map<String, Object> async = (Map<String, Object>) endpoint.info().get("async");
        assertThat(async).containsKeys("corePoolSize", "maxPoolSize", "queueCapacity", "activeThreads", "queueSize", "queueUsagePct");
    }

    @Test
    @DisplayName("Quando fila vazia deve retornar percentual de uso zero")
    void whenQueueEmptyShouldReturnZeroUsagePercent() {
        @SuppressWarnings("unchecked")
        Map<String, Object> async = (Map<String, Object>) endpoint.info().get("async");
        assertThat(async.get("queueUsagePct")).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Quando fila com 250 de 500 itens deve calcular 50 por cento de uso")
    void whenQueueWith250Of500ItemsShouldCalculate50PercentUsage() {
        LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < 250; i++) queue.add(() -> {});
        when(threadPoolExecutor.getQueue()).thenReturn(queue);
        @SuppressWarnings("unchecked")
        Map<String, Object> async = (Map<String, Object>) endpoint.info().get("async");
        assertThat((double) async.get("queueUsagePct")).isEqualTo(50.0);
    }

    @Test
    @DisplayName("Quando threads ativas configuradas deve refletir no resultado")
    void whenActiveThreadsConfiguredShouldReflectInResult() {
        when(executor.getActiveCount()).thenReturn(3);
        @SuppressWarnings("unchecked")
        Map<String, Object> async = (Map<String, Object>) endpoint.info().get("async");
        assertThat(async.get("activeThreads")).isEqualTo(3);
    }

    @Test
    @DisplayName("Quando structuredOutput habilitado deve refletir no resultado")
    void whenStructuredOutputEnabledShouldReflectInResult() {
        when(props.isStructuredOutput()).thenReturn(true);
        assertThat(endpoint.info()).containsEntry("structuredOutput", true);
    }
}
