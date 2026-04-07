package br.com.gbs.aspecta.logger.masking;

/**
 * Strategy interface for applying custom masking rules to serialized log content.
 * <p>
 * Register any number of implementations as Spring beans — the {@link br.com.gbs.aspecta.logger.utils.SensitiveDataMasker}
 * will discover and apply all of them in order, after the built-in key-based masking.
 * <p>
 * Example use cases: credit card number patterns, CPF/CNPJ formats, e-mail partial masking.
 *
 * <pre>{@code
 * @Component
 * public class CpfMaskingStrategy implements MaskingStrategy {
 *     // Matches bare CPF pattern: 123.456.789-00 or 12345678900
 *     private static final Pattern CPF = Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
 *
 *     @Override
 *     public String apply(String input) {
 *         return CPF.matcher(input).replaceAll("***.***.***-**");
 *     }
 * }
 * }</pre>
 */
public interface MaskingStrategy {

    /**
     * Applies masking to the given serialized string.
     *
     * @param input the serialized argument, return value, or exception message
     * @return the masked version of the input; never {@code null}
     */
    String apply(String input);
}
