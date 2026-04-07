package br.com.gbs.aspecta.logger.providers;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.interfaces.MessageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DelegatingMessageProvider implements MessageProvider {

    private final LoggerProperties loggerProperties;
    private final I18nMessageProvider i18nProvider;
    private final DefaultMessageProvider defaultProvider;

    private MessageProvider delegate() {
        return loggerProperties.isEnableI18n() ? i18nProvider : defaultProvider;
    }

    @Override
    public String entryMessage(String method, String args) {
        return delegate().entryMessage(method, args);
    }

    @Override
    public String exitMessage(String method, Object result) {
        return delegate().exitMessage(method, result);
    }

    @Override
    public String errorMessage(String method, String exceptionName, String message) {
        return delegate().errorMessage(method, exceptionName, message);
    }
}
