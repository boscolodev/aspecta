package br.com.gbs.aspecta.logger.interfaces;

public interface MessageProvider {
    String entryMessage(String method, String args);
    String exitMessage(String method, Object result);
    String errorMessage(String method, String exceptionName, String message);
}
