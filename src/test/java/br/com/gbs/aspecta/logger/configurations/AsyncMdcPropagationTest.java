package br.com.gbs.aspecta.logger.configurations;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AsyncLoggerConfig - propagação de MDC")
class AsyncMdcPropagationTest {

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    private ThreadPoolTaskExecutor buildExecutor() {
        LoggerProperties props = new LoggerProperties();
        AsyncLoggerConfig config = new AsyncLoggerConfig(props);
        return config.aspectaLoggerExecutor();
    }

    @Test
    @DisplayName("Quando traceId presente deve propagar para thread assíncrona")
    void whenTraceIdPresentShouldPropagateToAsyncThread() throws Exception {
        MDC.put("traceId", "trace-abc-123");
        ThreadPoolTaskExecutor exec = buildExecutor();
        CompletableFuture<String> future = new CompletableFuture<>();
        exec.execute(() -> future.complete(MDC.get("traceId")));
        String traceIdInThread = future.get(2, TimeUnit.SECONDS);
        assertThat(traceIdInThread).isEqualTo("trace-abc-123");
        exec.shutdown();
    }

    @Test
    @DisplayName("Quando múltiplos valores MDC devem todos ser propagados para thread assíncrona")
    void whenMultipleMdcValuesShouldAllBePropagatedToAsyncThread() throws Exception {
        MDC.put("traceId", "t1");
        MDC.put("spanId", "s1");
        MDC.put("userId", "u42");
        ThreadPoolTaskExecutor exec = buildExecutor();
        CompletableFuture<String> traceF = new CompletableFuture<>();
        CompletableFuture<String> spanF = new CompletableFuture<>();
        CompletableFuture<String> userF = new CompletableFuture<>();
        exec.execute(() -> {
            traceF.complete(MDC.get("traceId"));
            spanF.complete(MDC.get("spanId"));
            userF.complete(MDC.get("userId"));
        });
        assertThat(traceF.get(2, TimeUnit.SECONDS)).isEqualTo("t1");
        assertThat(spanF.get(2, TimeUnit.SECONDS)).isEqualTo("s1");
        assertThat(userF.get(2, TimeUnit.SECONDS)).isEqualTo("u42");
        exec.shutdown();
    }

    @Test
    @DisplayName("Quando tarefa concluída deve limpar MDC da thread assíncrona para próxima execução")
    void whenTaskCompletedShouldClearMdcInAsyncThreadForNextExecution() throws Exception {
        MDC.put("traceId", "cleanup-test");
        ThreadPoolTaskExecutor exec = buildExecutor();
        CompletableFuture<Void> firstDone = new CompletableFuture<>();
        CompletableFuture<String> after = new CompletableFuture<>();
        exec.execute(() -> firstDone.complete(null));
        firstDone.get(2, TimeUnit.SECONDS);
        MDC.clear();
        exec.execute(() -> after.complete(MDC.get("traceId")));
        String valueAfter = after.get(2, TimeUnit.SECONDS);
        assertThat(valueAfter).isNull();
        exec.shutdown();
    }

    @Test
    @DisplayName("Quando MDC vazio a thread assíncrona deve iniciar limpa")
    void whenMdcEmptyShouldAsyncThreadStartClean() throws Exception {
        ThreadPoolTaskExecutor exec = buildExecutor();
        CompletableFuture<String> future = new CompletableFuture<>();
        exec.execute(() -> future.complete(MDC.get("traceId")));
        assertThat(future.get(2, TimeUnit.SECONDS)).isNull();
        exec.shutdown();
    }
}
