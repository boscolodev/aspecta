package br.com.gbs.aspecta.logger.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DefaultMessageProvider")
class DefaultMessageProviderTest {

    private DefaultMessageProvider provider;

    @BeforeEach
    void setUp() {
        provider = new DefaultMessageProvider();
    }

    @Test
    @DisplayName("Quando gerada mensagem de entrada deve conter nome do método e argumentos")
    void whenEntryMessageGeneratedShouldContainMethodNameAndArgs() {
        String message = provider.entryMessage("doSomething", "arg1");
        assertThat(message).contains("doSomething").contains("arg1");
    }

    @Test
    @DisplayName("Quando gerada mensagem de saída deve conter nome do método e resultado")
    void whenExitMessageGeneratedShouldContainMethodNameAndResult() {
        String message = provider.exitMessage("doSomething", "resultValue");
        assertThat(message).contains("doSomething").contains("resultValue");
    }

    @Test
    @DisplayName("Quando gerada mensagem de erro deve conter método, exceção e mensagem")
    void whenErrorMessageGeneratedShouldContainMethodExceptionAndMessage() {
        String message = provider.errorMessage("doSomething", "NullPointerException", "valor nulo");
        assertThat(message).contains("doSomething").contains("NullPointerException").contains("valor nulo");
    }
}
