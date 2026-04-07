package br.com.gbs.aspecta.logger.configurations;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LoggerProperties - validação")
class LoggerPropertiesValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    @DisplayName("Quando propriedades padrão devem ser válidas sem violações")
    void whenDefaultPropertiesShouldBeValidWithoutViolations() {
        LoggerProperties props = new LoggerProperties();
        Set<ConstraintViolation<LoggerProperties>> violations = validator.validate(props);
        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 21, 100})
    @DisplayName("Quando corePoolSize inválido deve gerar violação de constraint")
    void whenInvalidCorePoolSizeShouldGenerateConstraintViolation(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setCorePoolSize(value);
        assertThat(validator.validate(props)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 20})
    @DisplayName("Quando corePoolSize válido não deve gerar violações")
    void whenValidCorePoolSizeShouldNotGenerateViolations(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setCorePoolSize(value);
        assertThat(validator.validate(props)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5, 101, 500})
    @DisplayName("Quando maxPoolSize inválido deve gerar violação de constraint")
    void whenInvalidMaxPoolSizeShouldGenerateConstraintViolation(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setMaxPoolSize(value);
        assertThat(validator.validate(props)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100})
    @DisplayName("Quando maxPoolSize válido não deve gerar violações")
    void whenValidMaxPoolSizeShouldNotGenerateViolations(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setMaxPoolSize(value);
        assertThat(validator.validate(props)).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 5001, 10000})
    @DisplayName("Quando queueCapacity inválido deve gerar violação de constraint")
    void whenInvalidQueueCapacityShouldGenerateConstraintViolation(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setQueueCapacity(value);
        assertThat(validator.validate(props)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 500, 5000})
    @DisplayName("Quando queueCapacity válido não deve gerar violações")
    void whenValidQueueCapacityShouldNotGenerateViolations(int value) {
        LoggerProperties props = new LoggerProperties();
        props.getAsync().setQueueCapacity(value);
        assertThat(validator.validate(props)).isEmpty();
    }

    @Test
    @DisplayName("Quando propriedades padrão devem incluir chaves sensíveis expandidas")
    void whenDefaultPropertiesShouldIncludeExpandedSensitiveKeys() {
        LoggerProperties props = new LoggerProperties();
        assertThat(props.getSensitiveKeys())
                .contains("password", "cpf", "token", "secret", "apikey",
                           "accesstoken", "refreshtoken", "authorization",
                           "privatekey", "creditcard");
    }

    @Test
    @DisplayName("Quando criado o structuredOutput padrão deve ser false")
    void whenCreatedDefaultStructuredOutputShouldBeFalse() {
        assertThat(new LoggerProperties().isStructuredOutput()).isFalse();
    }

    @Test
    @DisplayName("Quando criado o exposeExceptionDetails padrão deve ser false")
    void whenCreatedDefaultExposeExceptionDetailsShouldBeFalse() {
        assertThat(new LoggerProperties().isExposeExceptionDetails()).isFalse();
    }
}
