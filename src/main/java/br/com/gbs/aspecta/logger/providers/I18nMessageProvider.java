package br.com.gbs.aspecta.logger.providers;

import br.com.gbs.aspecta.logger.interfaces.I18nLogger;
import br.com.gbs.aspecta.logger.interfaces.MessageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class I18nMessageProvider implements MessageProvider {
    private final I18nLogger i18nLogger;

    @Override
    public String entryMessage(String method, String args) {
        return i18nLogger.getMessage("log.entry", new Object[]{method, args}, Locale.getDefault());
    }

    @Override
    public String exitMessage(String method, Object result) {
        return i18nLogger.getMessage("log.exit", new Object[]{method, result}, Locale.getDefault());
    }

    @Override
    public String errorMessage(String method, String exceptionName, String message) {
        return i18nLogger.getMessage("log.error", new Object[]{method, exceptionName, message}, Locale.getDefault());
    }
}
