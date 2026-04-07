package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Sanitização dos Exception Handlers")
class ExceptionHandlerSanitizationTest {

    @Mock private MessageSource messageSource;
    @Mock private LoggerProperties loggerProperties;
    @Mock private HttpServletRequest request;

    @InjectMocks private GlobalExceptionHandler globalHandler;
    @InjectMocks private DataExceptionHandler dataHandler;

    @BeforeEach
    void setUp() {
        when(request.getLocale()).thenReturn(Locale.getDefault());
    }

    @Test
    @DisplayName("Quando corpo da requisição não pode ser lido deve não expor detalhes de parse")
    void whenRequestBodyUnreadableShouldNotExposeParseDetails() {
        when(messageSource.getMessage(eq("exception.unreadable.message"), any(), any(Locale.class)))
                .thenReturn("Request body could not be read");
        ResponseEntity<?> response = globalHandler.handleUnreadableMessage(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        String body = response.getBody().toString();
        assertThat(body).doesNotContainIgnoringCase("JsonParseException");
        assertThat(body).doesNotContainIgnoringCase("Unexpected character");
        assertThat(body).doesNotContainIgnoringCase("at [Source");
    }

    @Test
    @DisplayName("Quando violação de integridade de dados deve não expor detalhes de constraint")
    void whenDataIntegrityViolationShouldNotExposeConstraintDetails() {
        when(messageSource.getMessage(eq("exception.data.integrity"), any(), any(Locale.class)))
                .thenReturn("Data integrity error");
        ResponseEntity<?> response = dataHandler.handleDataIntegrity(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        String body = response.getBody().toString();
        assertThat(body).doesNotContainIgnoringCase("constraint");
        assertThat(body).doesNotContainIgnoringCase("duplicate key");
        assertThat(body).doesNotContainIgnoringCase("table");
        assertThat(body).doesNotContainIgnoringCase("column");
    }

    @Test
    @DisplayName("Quando mensagem i18n ausente para data integrity deve usar a chave como fallback")
    void whenDataIntegrityMessageMissingShouldFallBackToKey() {
        when(messageSource.getMessage(eq("exception.data.integrity"), any(), any(Locale.class)))
                .thenThrow(new NoSuchMessageException("exception.data.integrity"));
        ResponseEntity<?> response = dataHandler.handleDataIntegrity(request);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }
}
