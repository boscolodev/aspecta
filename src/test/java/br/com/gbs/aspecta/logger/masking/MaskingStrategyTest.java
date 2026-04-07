package br.com.gbs.aspecta.logger.masking;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.utils.SensitiveDataMasker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MaskingStrategy - estratégias customizadas")
class MaskingStrategyTest {

    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern CARD_PATTERN = Pattern.compile("\\b\\d{4}[- ]?\\d{4}[- ]?\\d{4}[- ]?\\d{4}\\b");

    private static final MaskingStrategy CPF_STRATEGY =
            input -> CPF_PATTERN.matcher(input).replaceAll("***.***.***-**");

    private static final MaskingStrategy CARD_STRATEGY =
            input -> CARD_PATTERN.matcher(input).replaceAll("****-****-****-****");

    private SensitiveDataMasker maskerWith(MaskingStrategy... strategies) {
        LoggerProperties props = new LoggerProperties();
        props.setSensitiveKeys(List.of("password", "token"));
        return new SensitiveDataMasker(props, List.of(strategies));
    }

    @Test
    @DisplayName("Quando estratégia CPF aplicada deve mascarar CPF no formato com pontos")
    void whenCpfStrategyAppliedShouldMaskCpfWithDotFormat() {
        SensitiveDataMasker m = maskerWith(CPF_STRATEGY);
        String result = m.sanitizeArgs(new Object[]{"CPF: 123.456.789-00"}, true);
        assertThat(result).doesNotContain("123.456.789-00");
        assertThat(result).contains("***.***.***-**");
    }

    @Test
    @DisplayName("Quando estratégia CPF aplicada deve mascarar CPF no formato sem separadores")
    void whenCpfStrategyAppliedShouldMaskCpfWithoutSeparators() {
        SensitiveDataMasker m = maskerWith(CPF_STRATEGY);
        String result = m.sanitizeArgs(new Object[]{"12345678900"}, true);
        assertThat(result).doesNotContain("12345678900");
    }

    @Test
    @DisplayName("Quando estratégia de cartão aplicada deve mascarar número de cartão de crédito")
    void whenCardStrategyAppliedShouldMaskCreditCardNumber() {
        SensitiveDataMasker m = maskerWith(CARD_STRATEGY);
        String result = m.sanitizeArgs(new Object[]{"Card: 1234-5678-9012-3456"}, true);
        assertThat(result).doesNotContain("1234-5678-9012-3456");
        assertThat(result).contains("****-****-****-****");
    }

    @Test
    @DisplayName("Quando múltiplas estratégias devem ser aplicadas na ordem registrada")
    void whenMultipleStrategiesShouldBeAppliedInRegisteredOrder() {
        MaskingStrategy first = input -> input.replace("FIRST", "ONE");
        MaskingStrategy second = input -> input.replace("ONE", "TWO");
        SensitiveDataMasker m = maskerWith(first, second);
        String result = m.sanitizeArgs(new Object[]{"FIRST"}, false);
        assertThat(result).isEqualTo("TWO");
    }

    @Test
    @DisplayName("Quando estratégia aplicada deve também mascarar em sanitizeMessage")
    void whenStrategyAppliedShouldAlsoMaskInSanitizeMessage() {
        SensitiveDataMasker m = maskerWith(CPF_STRATEGY);
        String result = m.sanitizeMessage("User CPF 123.456.789-00 failed login", true, List.of());
        assertThat(result).doesNotContain("123.456.789-00");
    }

    @Test
    @DisplayName("Quando estratégia aplicada deve também mascarar em sanitizeResult")
    void whenStrategyAppliedShouldAlsoMaskInSanitizeResult() {
        SensitiveDataMasker m = maskerWith(CPF_STRATEGY);
        String result = m.sanitizeResult("CPF: 123.456.789-00", true);
        assertThat(result).doesNotContain("123.456.789-00");
    }

    @Test
    @DisplayName("Quando estratégia não encontra correspondência deve retornar entrada sem alteração")
    void whenStrategyFindsNoMatchShouldReturnInputUnchanged() {
        SensitiveDataMasker m = maskerWith(CPF_STRATEGY);
        String result = m.sanitizeArgs(new Object[]{"No sensitive data here"}, true);
        assertThat(result).contains("No sensitive data here");
    }

    @Test
    @DisplayName("Quando sem estratégias customizadas o mascaramento por chave ainda deve funcionar")
    void whenNoCustomStrategiesKeyMaskingShouldStillWork() {
        LoggerProperties props = new LoggerProperties();
        props.setSensitiveKeys(List.of("password"));
        SensitiveDataMasker m = new SensitiveDataMasker(props, List.of());
        String result = m.sanitizeArgs(new Object[]{"{\"password\":\"pw\"}"}, true);
        assertThat(result).doesNotContain("pw");
    }
}
