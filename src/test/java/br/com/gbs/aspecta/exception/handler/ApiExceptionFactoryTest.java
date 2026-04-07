package br.com.gbs.aspecta.exception.handler;

import br.com.gbs.aspecta.exception.ExceptionType;
import br.com.gbs.aspecta.exception.exception.ApiErrorException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ApiExceptionFactory")
class ApiExceptionFactoryTest {

    @Test
    @DisplayName("Quando troll com tipo e mensagem deve lançar ApiErrorException com 500")
    void whenTrollWithTypeAndMessageShouldThrowWith500() {
        RuntimeException cause = new RuntimeException("causa");
        assertThatThrownBy(() -> ApiExceptionFactory.troll(ExceptionType.BASIC, "erro", cause))
                .isInstanceOf(ApiErrorException.class)
                .hasMessage("erro");
    }

    @Test
    @DisplayName("Quando troll com status deve lançar ApiErrorException com status informado")
    void whenTrollWithStatusShouldThrowWithGivenStatus() {
        assertThatThrownBy(() -> ApiExceptionFactory.troll(ExceptionType.BASIC, "erro", HttpStatus.NOT_FOUND))
                .isInstanceOf(ApiErrorException.class)
                .extracting(e -> ((ApiErrorException) e).getStatus())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("Quando troll com status e causa deve lançar ApiErrorException com ambos")
    void whenTrollWithStatusAndCauseShouldThrowWithBoth() {
        RuntimeException cause = new RuntimeException("causa");
        assertThatThrownBy(() -> ApiExceptionFactory.troll(ExceptionType.COMPLETE, "erro", HttpStatus.CONFLICT, cause))
                .isInstanceOf(ApiErrorException.class)
                .hasCause(cause);
    }

    @Test
    @DisplayName("Quando troll com código de erro deve lançar ApiErrorException com errorCode")
    void whenTrollWithErrorCodeShouldThrowWithErrorCode() {
        assertThatThrownBy(() -> ApiExceptionFactory.troll(ExceptionType.BASIC, "erro", HttpStatus.BAD_REQUEST, "ERR_001"))
                .isInstanceOf(ApiErrorException.class)
                .extracting(e -> ((ApiErrorException) e).getErrorCode())
                .isEqualTo("ERR_001");
    }

    @Test
    @DisplayName("Quando troll com código de erro e causa deve lançar ApiErrorException com ambos")
    void whenTrollWithErrorCodeAndCauseShouldThrowWithBoth() {
        RuntimeException cause = new RuntimeException("causa");
        assertThatThrownBy(() -> ApiExceptionFactory.troll(ExceptionType.BASIC, "erro", HttpStatus.BAD_REQUEST, "ERR_002", cause))
                .isInstanceOf(ApiErrorException.class)
                .extracting(e -> ((ApiErrorException) e).getErrorCode())
                .isEqualTo("ERR_002");
    }

    @Test
    @DisplayName("Quando trollI18n deve lançar ApiErrorException com chave i18n e código de erro")
    void whenTrollI18nShouldThrowWithI18nKeyAndErrorCode() {
        assertThatThrownBy(() -> ApiExceptionFactory.trollI18n(
                ExceptionType.BASIC, "msg.key", "fallback", HttpStatus.NOT_FOUND, "NF_001"))
                .isInstanceOf(ApiErrorException.class)
                .extracting(e -> ((ApiErrorException) e).getMessageKey())
                .isEqualTo("msg.key");
    }
}
