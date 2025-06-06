package br.com.gbs.aspecta.logger.service;

import br.com.gbs.aspecta.logger.interfaces.AsyncLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncLoggerService implements AsyncLogger {

    @Async
    public void logInfo(String message, Object... args) {
        log.info(message, args);
    }

    @Async
    public void logError(String message, Object... args) {
        log.error(message, args);
    }
}
