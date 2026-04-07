package br.com.gbs.aspecta.logger.utils;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.masking.MaskingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SensitiveDataMasker")
class SensitiveDataMaskerTest {

    private LoggerProperties props;
    private SensitiveDataMasker masker;

    @BeforeEach
    void setUp() {
        props = new LoggerProperties();
        props.setSensitiveKeys(List.of("password", "token", "cpf"));
        masker = new SensitiveDataMasker(props, null);
    }

    @Nested
    @DisplayName("sanitizeArgs - JSON")
    class SanitizeArgsJson {

        @Test
        @DisplayName("Quando valor string de chave sensível deve mascarar")
        void whenSensitiveKeyStringValueShouldMask() {
            String json = "{\"password\":\"secret123\",\"name\":\"João\"}";
            String result = masker.sanitizeArgs(new Object[]{json}, true);
            assertThat(result).contains("***").doesNotContain("secret123");
            assertThat(result).contains("João");
        }

        @Test
        @DisplayName("Quando valor numérico de chave sensível deve mascarar")
        void whenSensitiveKeyNumericValueShouldMask() {
            String json = "{\"cpf\":12345678900,\"name\":\"Ana\"}";
            String result = masker.sanitizeArgs(new Object[]{json}, true);
            assertThat(result).doesNotContain("12345678900");
            assertThat(result).contains("Ana");
        }

        @Test
        @DisplayName("Quando chave excluída do mask deve permanecer visível")
        void whenKeyExcludedFromMaskShouldRemainVisible() {
            String json = "{\"password\":\"secret\",\"token\":\"tok123\"}";
            String result = masker.sanitizeArgs(new Object[]{json}, true, List.of("token"));
            assertThat(result).contains("tok123");
            assertThat(result).doesNotContain("secret");
        }

        @Test
        @DisplayName("Quando mascaramento desabilitado deve retornar valor sem mascaramento")
        void whenMaskingDisabledShouldReturnValueWithoutMasking() {
            String json = "{\"password\":\"secret123\"}";
            String result = masker.sanitizeArgs(new Object[]{json}, false);
            assertThat(result).contains("secret123");
        }

        @Test
        @DisplayName("Quando argumento nulo deve retornar a string literal null")
        void whenNullArgShouldReturnLiteralNull() {
            String result = masker.sanitizeArgs(new Object[]{null}, true);
            assertThat(result).isEqualTo("null");
        }

        @Test
        @DisplayName("Quando múltiplos argumentos devem mascarar todos os sensíveis")
        void whenMultipleArgsShouldMaskAllSensitiveValues() {
            String json1 = "{\"password\":\"p1\"}";
            String json2 = "{\"token\":\"t1\"}";
            String result = masker.sanitizeArgs(new Object[]{json1, json2}, true);
            assertThat(result).doesNotContain("p1").doesNotContain("t1");
        }
    }

    @Nested
    @DisplayName("sanitizeArgs - arrays e coleções")
    class SanitizeArgsCollections {

        @Test
        @DisplayName("Quando array de objetos deve serializar corretamente")
        void whenObjectArrayShouldSerializeCorrectly() {
            Object[] arg = new Object[]{"a", "b"};
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("[a, b]");
        }

        @Test
        @DisplayName("Quando array de inteiros deve serializar corretamente")
        void whenIntArrayShouldSerializeCorrectly() {
            int[] arg = {1, 2, 3};
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("[1, 2, 3]");
        }

        @Test
        @DisplayName("Quando array de longs deve serializar corretamente")
        void whenLongArrayShouldSerializeCorrectly() {
            long[] arg = {10L, 20L};
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("[10, 20]");
        }

        @Test
        @DisplayName("Quando array de doubles deve serializar corretamente")
        void whenDoubleArrayShouldSerializeCorrectly() {
            double[] arg = {1.5, 2.5};
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("[1.5, 2.5]");
        }

        @Test
        @DisplayName("Quando array de booleans deve serializar corretamente")
        void whenBooleanArrayShouldSerializeCorrectly() {
            boolean[] arg = {true, false};
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("[true, false]");
        }

        @Test
        @DisplayName("Quando coleção deve serializar corretamente")
        void whenCollectionShouldSerializeCorrectly() {
            List<String> arg = List.of("x", "y");
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("x").contains("y");
        }

        @Test
        @DisplayName("Quando mapa deve serializar corretamente")
        void whenMapShouldSerializeCorrectly() {
            Map<String, Integer> arg = Map.of("count", 42);
            String result = masker.sanitizeArgs(new Object[]{arg}, false);
            assertThat(result).contains("count").contains("42");
        }

        @Test
        @DisplayName("Quando JSON sensível dentro de array de objetos deve mascarar")
        void whenSensitiveJsonInsideObjectArrayShouldMask() {
            String sensitiveJson = "{\"password\":\"secret\"}";
            Object[] arg = new Object[]{sensitiveJson};
            String result = masker.sanitizeArgs(new Object[]{arg}, true);
            assertThat(result).doesNotContain("secret");
        }
    }

    @Nested
    @DisplayName("sanitizeResult")
    class SanitizeResult {

        @Test
        @DisplayName("Quando mascaramento habilitado deve mascarar resultado sensível")
        void whenMaskingEnabledShouldMaskSensitiveResult() {
            String json = "{\"token\":\"abc\"}";
            assertThat(masker.sanitizeResult(json, true)).doesNotContain("abc");
        }

        @Test
        @DisplayName("Quando mascaramento desabilitado deve retornar resultado sem alteração")
        void whenMaskingDisabledShouldReturnResultUnchanged() {
            String json = "{\"token\":\"abc\"}";
            assertThat(masker.sanitizeResult(json, false)).contains("abc");
        }

        @Test
        @DisplayName("Quando chave excluída do mask deve permanecer visível no resultado")
        void whenKeyExcludedFromMaskShouldRemainVisibleInResult() {
            String json = "{\"token\":\"abc\",\"password\":\"pw\"}";
            String result = masker.sanitizeResult(json, true, List.of("token"));
            assertThat(result).contains("abc");
            assertThat(result).doesNotContain("pw");
        }
    }

    @Nested
    @DisplayName("sanitizeMessage")
    class SanitizeMessage {

        @Test
        @DisplayName("Quando mensagem contém padrão JSON sensível deve mascarar")
        void whenMessageContainsSensitiveJsonPatternShouldMask() {
            String msg = "Error: {\"password\":\"hunter2\"} was rejected";
            String result = masker.sanitizeMessage(msg, true, List.of());
            assertThat(result).doesNotContain("hunter2");
        }

        @Test
        @DisplayName("Quando mensagem contém chave=valor deve mascarar o valor")
        void whenMessageContainsKeyEqualsValueShouldMaskValue() {
            String msg = "Login failed for password=hunter2 at service";
            String result = masker.sanitizeMessage(msg, true, List.of());
            assertThat(result).doesNotContain("hunter2");
            assertThat(result).contains("password=***");
        }

        @Test
        @DisplayName("Quando mensagem contém chave: valor deve mascarar o valor")
        void whenMessageContainsKeyColonValueShouldMaskValue() {
            String msg = "Rejected token: abc123 (expired)";
            String result = masker.sanitizeMessage(msg, true, List.of());
            assertThat(result).doesNotContain("abc123");
        }

        @Test
        @DisplayName("Quando mensagem nula deve retornar nulo")
        void whenNullMessageShouldReturnNull() {
            assertThat(masker.sanitizeMessage(null, true, List.of())).isNull();
        }

        @Test
        @DisplayName("Quando mascaramento desabilitado deve retornar mensagem original")
        void whenMaskingDisabledShouldReturnOriginalMessage() {
            String msg = "password=secret";
            assertThat(masker.sanitizeMessage(msg, false, List.of())).isEqualTo(msg);
        }

        @Test
        @DisplayName("Quando chave excluída do mask deve permanecer visível na mensagem")
        void whenKeyExcludedFromMaskShouldRemainVisibleInMessage() {
            String msg = "Error with token=abc123 and password=secret";
            String result = masker.sanitizeMessage(msg, true, List.of("token"));
            assertThat(result).contains("abc123");
            assertThat(result).doesNotContain("secret");
        }
    }

    @Nested
    @DisplayName("MaskingStrategy customizada")
    class CustomMaskingStrategy {

        @Test
        @DisplayName("Quando estratégia customizada deve ser aplicada após mascaramento por chave")
        void whenCustomStrategyShouldBeAppliedAfterKeyMasking() {
            MaskingStrategy cpfStrategy = input ->
                    input.replaceAll("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", "***.***.***-**");
            SensitiveDataMasker maskerWithStrategy = new SensitiveDataMasker(props, List.of(cpfStrategy));
            String result = maskerWithStrategy.sanitizeArgs(new Object[]{"User CPF: 123.456.789-00"}, true);
            assertThat(result).doesNotContain("123.456.789-00");
            assertThat(result).contains("***.***.***-**");
        }

        @Test
        @DisplayName("Quando múltiplas estratégias devem ser aplicadas na ordem registrada")
        void whenMultipleStrategiesShouldBeAppliedInRegisteredOrder() {
            MaskingStrategy s1 = input -> input.replace("A", "X");
            MaskingStrategy s2 = input -> input.replace("X", "Z");
            SensitiveDataMasker m = new SensitiveDataMasker(props, List.of(s1, s2));
            String result = m.sanitizeArgs(new Object[]{"ABC"}, false);
            assertThat(result).isEqualTo("ZBC");
        }

        @Test
        @DisplayName("Quando lista de estratégias nula deve funcionar apenas com mascaramento por chave")
        void whenStrategyListNullShouldWorkWithKeyMaskingOnly() {
            SensitiveDataMasker m = new SensitiveDataMasker(props, null);
            String json = "{\"password\":\"pw\"}";
            assertThat(m.sanitizeArgs(new Object[]{json}, true)).doesNotContain("pw");
        }
    }

    @Nested
    @DisplayName("toStringArg")
    class ToStringArg {

        @Test
        @DisplayName("Quando argumento nulo deve retornar a string literal null")
        void whenNullShouldReturnLiteralNull() {
            assertThat(masker.toStringArg(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("Quando array de floats deve serializar corretamente")
        void whenFloatArrayShouldSerializeCorrectly() {
            assertThat(masker.toStringArg(new float[]{1.0f, 2.0f})).contains("1.0").contains("2.0");
        }

        @Test
        @DisplayName("Quando array de bytes deve serializar corretamente")
        void whenByteArrayShouldSerializeCorrectly() {
            assertThat(masker.toStringArg(new byte[]{1, 2})).isNotBlank();
        }

        @Test
        @DisplayName("Quando array de chars deve serializar corretamente")
        void whenCharArrayShouldSerializeCorrectly() {
            assertThat(masker.toStringArg(new char[]{'a', 'b'})).contains("a").contains("b");
        }

        @Test
        @DisplayName("Quando array de shorts deve serializar corretamente")
        void whenShortArrayShouldSerializeCorrectly() {
            assertThat(masker.toStringArg(new short[]{1, 2})).contains("1").contains("2");
        }

        @Test
        @DisplayName("Quando objeto comum deve retornar toString do objeto")
        void whenRegularObjectShouldReturnObjectToString() {
            assertThat(masker.toStringArg("hello")).isEqualTo("hello");
        }
    }
}
