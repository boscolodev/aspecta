package br.com.gbs.aspecta.logger.interfaces;

public interface AsyncLogger {

    void logInfo(String message, Object... args);

    void logError(String message, Object... args);
}
