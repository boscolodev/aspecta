package br.com.gbs.aspecta.exception.exception;

import br.com.gbs.aspecta.exception.ExceptionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiErrorException")
class ApiErrorExceptionTest {

    @Test
    @DisplayName("Quando criada sem status deve usar INTERNAL_SERVER_ERROR como padrão")
    void whenCreatedWithoutStatusShouldDefaultToInternalServerError() {
        ApiErrorException ex = new ApiErrorException(ExceptionType.BASIC, "erro simples");
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(ex.getErrorCode()).isNull();
        assertThat(ex.getMessageKey()).isNull();
        assertThat(ex.getMessage()).isEqualTo("erro simples");
    }

    @Test
    @DisplayName("Quando criada com status e causa deve propagar status e mensagem")
    void whenCreatedWithStatusAndCauseShouldPropagateStatusAndMessage() {
        RuntimeException cause = new RuntimeException("causa raiz");
        ApiErrorException ex = new ApiErrorException(ExceptionType.COMPLETE, "erro", HttpStatus.BAD_REQUEST, cause);
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getCause()).isSameAs(cause);
        assertThat(ex.getErrorCode()).isNull();
    }

    @Test
    @DisplayName("Quando criada com causa ApiErrorException deve herdar o status da causa")
    void whenCreatedWithApiErrorCauseShouldInheritCauseStatus() {
        ApiErrorException cause = new ApiErrorException(ExceptionType.BASIC, "causa", HttpStatus.UNPROCESSABLE_ENTITY);
        ApiErrorException wrapper = new ApiErrorException(ExceptionType.BASIC, "wrapper", cause);
        assertThat(wrapper.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @DisplayName("Quando criada com causa não-ApiErrorException deve usar INTERNAL_SERVER_ERROR")
    void whenCreatedWithNonApiErrorCauseShouldUseInternalServerError() {
        RuntimeException cause = new RuntimeException("causa comum");
        ApiErrorException ex = new ApiErrorException(ExceptionType.BASIC, "erro", cause);
        assertThat(ex.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
