package br.com.gbs.aspecta.logger.providers;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DelegatingMessageProvider")
class DelegatingMessageProviderTest {

    @Mock private LoggerProperties loggerProperties;
    @Mock private I18nMessageProvider i18nProvider;
    @Mock private DefaultMessageProvider defaultProvider;

    private DelegatingMessageProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DelegatingMessageProvider(loggerProperties, i18nProvider, defaultProvider);
    }

    @Test
    @DisplayName("Quando i18n habilitado deve delegar entryMessage ao provedor i18n")
    void whenI18nEnabledShouldDelegateEntryMessageToI18nProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(true);
        provider.entryMessage("method", "args");
        verify(i18nProvider).entryMessage("method", "args");
    }

    @Test
    @DisplayName("Quando i18n desabilitado deve delegar entryMessage ao provedor padrão")
    void whenI18nDisabledShouldDelegateEntryMessageToDefaultProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(false);
        provider.entryMessage("method", "args");
        verify(defaultProvider).entryMessage("method", "args");
    }

    @Test
    @DisplayName("Quando i18n habilitado deve delegar exitMessage ao provedor i18n")
    void whenI18nEnabledShouldDelegateExitMessageToI18nProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(true);
        provider.exitMessage("method", "result");
        verify(i18nProvider).exitMessage("method", "result");
    }

    @Test
    @DisplayName("Quando i18n desabilitado deve delegar exitMessage ao provedor padrão")
    void whenI18nDisabledShouldDelegateExitMessageToDefaultProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(false);
        provider.exitMessage("method", "result");
        verify(defaultProvider).exitMessage("method", "result");
    }

    @Test
    @DisplayName("Quando i18n habilitado deve delegar errorMessage ao provedor i18n")
    void whenI18nEnabledShouldDelegateErrorMessageToI18nProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(true);
        provider.errorMessage("method", "Exception", "msg");
        verify(i18nProvider).errorMessage("method", "Exception", "msg");
    }

    @Test
    @DisplayName("Quando i18n desabilitado deve delegar errorMessage ao provedor padrão")
    void whenI18nDisabledShouldDelegateErrorMessageToDefaultProvider() {
        when(loggerProperties.isEnableI18n()).thenReturn(false);
        provider.errorMessage("method", "Exception", "msg");
        verify(defaultProvider).errorMessage("method", "Exception", "msg");
    }
}
