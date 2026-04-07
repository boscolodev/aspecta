package br.com.gbs.aspecta.logger.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("I18NLoggerService")
class I18NLoggerServiceTest {

    @Mock private MessageSource messageSource;

    private I18NLoggerService service;

    @BeforeEach
    void setUp() {
        service = new I18NLoggerService(messageSource);
    }

    @Test
    @DisplayName("Quando getMessage chamado deve delegar ao MessageSource com chave e locale")
    void whenGetMessageCalledShouldDelegateToMessageSource() {
        when(messageSource.getMessage(eq("log.entry"), any(), eq(Locale.getDefault())))
                .thenReturn("Entrando no método");
        String result = service.getMessage("log.entry", null, Locale.getDefault());
        assertThat(result).isEqualTo("Entrando no método");
    }

    @Test
    @DisplayName("Quando getMessage chamado com argumentos deve repassá-los ao MessageSource")
    void whenGetMessageCalledWithArgsShouldPassThemToMessageSource() {
        Object[] args = {"doSomething", "valor"};
        when(messageSource.getMessage(eq("log.entry"), eq(args), eq(Locale.ENGLISH)))
                .thenReturn("mensagem formatada");
        String result = service.getMessage("log.entry", args, Locale.ENGLISH);
        assertThat(result).isEqualTo("mensagem formatada");
    }
}
