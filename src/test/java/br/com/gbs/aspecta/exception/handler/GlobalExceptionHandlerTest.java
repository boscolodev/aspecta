package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.dto.BasicResponse;
import br.com.gbs.aspecta.exception.dto.CompleteResponse;
import br.com.gbs.aspecta.exception.dto.FieldMessage;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Mock private MessageSource messageSource;
    @Mock private LoggerProperties loggerProperties;
    @Mock private HttpServletRequest request;
    @InjectMocks private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        when(request.getLocale()).thenReturn(Locale.getDefault());
        when(request.getRequestURI()).thenReturn("/test/path");
    }

    @Nested
    @DisplayName("Exceções BASIC")
    class BasicExceptions {

        @Test
        @DisplayName("Quando ApiErrorException BASIC lançada deve retornar 400 com código de erro")
        void whenBasicExceptionThrownShouldReturn400WithErrorCode() {
            ApiErrorException ex = new ApiErrorException(ExceptionType.BASIC, "Bad input", HttpStatus.BAD_REQUEST, "BAD_INPUT");
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            BasicResponse body = (BasicResponse) response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.getErrorCode()).isEqualTo("BAD_INPUT");
            assertThat(body.getMessage()).isEqualTo("Bad input");
        }

        @Test
        @DisplayName("Quando chave i18n presente deve resolver mensagem via MessageSource")
        void whenI18nKeyPresentShouldResolveMessageViaMessageSource() {
            when(messageSource.getMessage(eq("err.key"), any(), any(Locale.class))).thenReturn("Translated message");
            ApiErrorException ex = new ApiErrorException(ExceptionType.BASIC, "err.key", "Fallback", HttpStatus.NOT_FOUND, "NF");
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            BasicResponse body = (BasicResponse) response.getBody();
            assertThat(body.getMessage()).isEqualTo("Translated message");
        }

        @Test
        @DisplayName("Quando chave i18n ausente deve usar a mensagem de fallback")
        void whenI18nKeyMissingShouldUseFallbackMessage() {
            when(messageSource.getMessage(eq("missing.key"), any(), any(Locale.class)))
                    .thenThrow(new NoSuchMessageException("missing.key"));
            ApiErrorException ex = new ApiErrorException(ExceptionType.BASIC, "missing.key", "Fallback msg", HttpStatus.NOT_FOUND, "NF");
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            BasicResponse body = (BasicResponse) response.getBody();
            assertThat(body.getMessage()).isEqualTo("Fallback msg");
        }

        @Test
        @DisplayName("Quando exceção aninhada contém status deve extraí-lo da causa")
        void whenNestedExceptionContainsStatusShouldExtractFromCause() {
            ApiErrorException cause = new ApiErrorException(ExceptionType.BASIC, "root", HttpStatus.UNPROCESSABLE_ENTITY);
            ApiErrorException wrapper = new ApiErrorException(ExceptionType.BASIC, "wrapper", cause);
            ResponseEntity<?> response = handler.handleApiErrorException(wrapper, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    @Nested
    @DisplayName("Exceções COMPLETE")
    class CompleteExceptions {

        @Test
        @DisplayName("Quando detalhes não expostos deve retornar null nos detalhes")
        void whenExposeDisabledShouldReturnNullDetails() {
            when(loggerProperties.isExposeExceptionDetails()).thenReturn(false);
            RuntimeException cause = new RuntimeException("db error detail");
            ApiErrorException ex = new ApiErrorException(ExceptionType.COMPLETE, "Internal", HttpStatus.INTERNAL_SERVER_ERROR, "IE", cause);
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            CompleteResponse body = (CompleteResponse) response.getBody();
            assertThat(body.getDetails()).isNull();
        }

        @Test
        @DisplayName("Quando detalhes expostos e causa presente deve incluir mensagem da causa")
        void whenExposeEnabledAndCausePresentShouldIncludeCauseMessage() {
            when(loggerProperties.isExposeExceptionDetails()).thenReturn(true);
            RuntimeException cause = new RuntimeException("visible cause");
            ApiErrorException ex = new ApiErrorException(ExceptionType.COMPLETE, "Internal", HttpStatus.INTERNAL_SERVER_ERROR, "IE", cause);
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            CompleteResponse body = (CompleteResponse) response.getBody();
            assertThat(body.getDetails()).isEqualTo("visible cause");
        }

        @Test
        @DisplayName("Quando detalhes expostos mas sem causa deve retornar null nos detalhes")
        void whenExposeEnabledButNoCauseShouldReturnNullDetails() {
            when(loggerProperties.isExposeExceptionDetails()).thenReturn(true);
            ApiErrorException ex = new ApiErrorException(ExceptionType.COMPLETE, "Err", HttpStatus.INTERNAL_SERVER_ERROR);
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            CompleteResponse body = (CompleteResponse) response.getBody();
            assertThat(body.getDetails()).isNull();
        }

        @Test
        @DisplayName("Quando exceção COMPLETE lançada deve incluir caminho e timestamp na resposta")
        void whenCompleteExceptionThrownShouldIncludePathAndTimestamp() {
            when(loggerProperties.isExposeExceptionDetails()).thenReturn(false);
            ApiErrorException ex = new ApiErrorException(ExceptionType.COMPLETE, "Err", HttpStatus.INTERNAL_SERVER_ERROR);
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            CompleteResponse body = (CompleteResponse) response.getBody();
            assertThat(body.getPath()).isEqualTo("/test/path");
            assertThat(body.getTimestamp()).isNotBlank();
        }
    }

    @Nested
    @DisplayName("Exceções PROBLEM_DETAIL")
    class ProblemDetailExceptions {

        @Test
        @DisplayName("Quando PROBLEM_DETAIL deve retornar formato RFC 7807")
        void whenProblemDetailShouldReturnRfc7807Format() {
            ApiErrorException ex = new ApiErrorException(ExceptionType.PROBLEM_DETAIL, "Unprocessable", HttpStatus.UNPROCESSABLE_ENTITY, "UP");
            ResponseEntity<?> response = handler.handleApiErrorException(ex, request);
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);
            ProblemDetail pd = (ProblemDetail) response.getBody();
            assertThat(pd.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
            assertThat(pd.getProperties()).containsKey("errorCode");
        }

        @Test
        @DisplayName("Quando PROBLEM_DETAIL a instância deve conter a URI da requisição")
        void whenProblemDetailShouldContainRequestUri() {
            ApiErrorException ex = new ApiErrorException(ExceptionType.PROBLEM_DETAIL, "Err", HttpStatus.UNPROCESSABLE_ENTITY);
            ProblemDetail pd = (ProblemDetail) handler.handleApiErrorException(ex, request).getBody();
            assertThat(pd.getInstance().toString()).isEqualTo("/test/path");
        }
    }

    @Nested
    @DisplayName("Exceções Spring padrão")
    class SpringExceptions {

        @Test
        @DisplayName("Quando corpo ilegível deve retornar 400")
        void whenUnreadableBodyShouldReturn400() {
            when(messageSource.getMessage(eq("exception.unreadable.message"), any(), any(Locale.class))).thenReturn("Cannot read body");
            ResponseEntity<?> response = handler.handleUnreadableMessage(request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(((BasicResponse) response.getBody()).getErrorCode()).isEqualTo("UNREADABLE_REQUEST_BODY");
        }

        @Test
        @DisplayName("Quando handler não encontrado deve retornar 404")
        void whenHandlerNotFoundShouldReturn404() {
            when(messageSource.getMessage(eq("exception.handler.not.found"), any(), any(Locale.class))).thenReturn("Not found");
            ResponseEntity<?> response = handler.handleNotFound(request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(((BasicResponse) response.getBody()).getErrorCode()).isEqualTo("HANDLER_NOT_FOUND");
        }

        @Test
        @DisplayName("Quando método HTTP não suportado deve retornar 405")
        void whenMethodNotAllowedShouldReturn405() {
            when(messageSource.getMessage(eq("exception.method.not.allowed"), any(), any(Locale.class))).thenReturn("Not allowed");
            ResponseEntity<?> response = handler.handleMethodNotAllowed(request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(((BasicResponse) response.getBody()).getErrorCode()).isEqualTo("METHOD_NOT_ALLOWED");
        }

        @Test
        @DisplayName("Quando mensagem i18n ausente deve usar a chave como mensagem de fallback")
        void whenI18nMessageMissingShouldUseFallbackKey() {
            when(messageSource.getMessage(anyString(), any(), any(Locale.class))).thenThrow(new NoSuchMessageException("k"));
            ResponseEntity<?> response = handler.handleNotFound(request);
            assertThat(((BasicResponse) response.getBody()).getMessage()).isEqualTo("exception.handler.not.found");
        }
    }

    @Nested
    @DisplayName("Exceções de validação")
    class ValidationExceptions {

        @Test
        @DisplayName("Quando validação falha deve retornar 400 com erros de campo")
        void whenValidationFailsShouldReturn400WithFieldErrors() {
            when(messageSource.getMessage(eq("exception.validation.error"), any(), any(Locale.class))).thenReturn("Validation error");
            FieldError fieldError = new FieldError("obj", "email", "must be valid");
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
            MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
            when(ex.getBindingResult()).thenReturn(bindingResult);
            ResponseEntity<?> response = handler.handleValidationException(ex, request);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            FieldMessage body = (FieldMessage) response.getBody();
            assertThat(body.getMessage()).isEqualTo("Validation error");
            assertThat(body.getDetails()).isNotNull();
        }
    }
}
