package br.com.gbs.aspecta.logger.interfaces;

public interface AsyncLogger {

    void logDebug(String message, Object... args);

    void logInfo(String message, Object... args);

    void logWarn(String message, Object... args);

    void logError(String message, Object... args);
}
