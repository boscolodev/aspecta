package br.com.gbs.aspecta.logger.providers;

import br.com.gbs.aspecta.logger.interfaces.I18nLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("I18nMessageProvider")
class I18nMessageProviderTest {

    @Mock private I18nLogger i18nLogger;

    private I18nMessageProvider provider;

    @BeforeEach
    void setUp() {
        provider = new I18nMessageProvider(i18nLogger);
    }

    @Test
    @DisplayName("Quando entryMessage chamado deve usar chave log.entry no i18n")
    void whenEntryMessageCalledShouldUseLogEntryKey() {
        provider.entryMessage("doSomething", "args");
        verify(i18nLogger).getMessage(eq("log.entry"), any(Object[].class), any(Locale.class));
    }

    @Test
    @DisplayName("Quando exitMessage chamado deve usar chave log.exit no i18n")
    void whenExitMessageCalledShouldUseLogExitKey() {
        provider.exitMessage("doSomething", "result");
        verify(i18nLogger).getMessage(eq("log.exit"), any(Object[].class), any(Locale.class));
    }

    @Test
    @DisplayName("Quando errorMessage chamado deve usar chave log.error no i18n")
    void whenErrorMessageCalledShouldUseLogErrorKey() {
        provider.errorMessage("doSomething", "RuntimeException", "falhou");
        verify(i18nLogger).getMessage(eq("log.error"), any(Object[].class), any(Locale.class));
    }
}
