package br.com.gbs.aspecta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@DisplayName("AspectaApplication")
class AspectaApplicationTests {

    @Test
    @DisplayName("Quando contexto Spring inicializado deve carregar todos os beans sem erros")
    void whenSpringContextStartedShouldLoadAllBeansWithoutErrors() {
    }
}
