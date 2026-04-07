package br.com.gbs.aspecta.logger.utils;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import br.com.gbs.aspecta.logger.masking.MaskingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SensitiveDataMasker {

    private final LoggerProperties loggerProperties;
    private final List<MaskingStrategy> maskingStrategies;

    public SensitiveDataMasker(LoggerProperties loggerProperties,
                                @Autowired(required = false) List<MaskingStrategy> maskingStrategies) {
        this.loggerProperties = loggerProperties;
        this.maskingStrategies = maskingStrategies != null ? maskingStrategies : List.of();
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public String sanitizeArgs(Object[] args, boolean applyMasking) {
        return sanitizeArgs(args, applyMasking, List.of());
    }

    public String sanitizeArgs(Object[] args, boolean applyMasking, List<String> excludeFromMask) {
        if (!applyMasking) {
            String serialized = Arrays.stream(args)
                    .map(this::toStringArg)
                    .collect(Collectors.joining(", "));
            return applyStrategies(serialized);
        }
        List<String> keys = effectiveKeys(excludeFromMask);
        return Arrays.stream(args)
                .map(arg -> applyJsonMasking(toStringArg(arg), keys))
                .collect(Collectors.joining(", "));
    }

    public String sanitizeResult(Object result, boolean applyMasking) {
        return sanitizeArgs(new Object[]{result}, applyMasking, List.of());
    }

    public String sanitizeResult(Object result, boolean applyMasking, List<String> excludeFromMask) {
        return sanitizeArgs(new Object[]{result}, applyMasking, excludeFromMask);
    }

    /**
     * Sanitizes a plain-text message (e.g., an exception message) by applying
     * key-based masking in both JSON and plain-text formats, followed by any
     * registered {@link MaskingStrategy} beans.
     *
     * @param message        the raw message; if {@code null}, returns {@code null}
     * @param applyMasking   when {@code false} returns the original message unchanged
     * @param excludeFromMask field names that must NOT be masked
     */
    public String sanitizeMessage(String message, boolean applyMasking, List<String> excludeFromMask) {
        if (message == null) return null;
        if (!applyMasking) return message;
        List<String> keys = effectiveKeys(excludeFromMask);
        String result = applyJsonMasking(message, keys);
        result = applyPlainTextMasking(result, keys);
        return result;
    }

    // ── Core masking ──────────────────────────────────────────────────────────

    private String applyJsonMasking(String input, List<String> keys) {
        String result = input;
        for (String key : keys) {
            String qk = Pattern.quote(key);
            // "key": "string value"
            result = result.replaceAll("(?i)(\"" + qk + "\"\\s*:\\s*\")[^\"]*\"", "$1***\"");
            // "key": numeric
            result = result.replaceAll("(?i)(\"" + qk + "\"\\s*:\\s*)(-?\\d+(?:\\.\\d+)?)", "$1\"***\"");
        }
        return applyStrategies(result);
    }

    private String applyPlainTextMasking(String input, List<String> keys) {
        String result = input;
        for (String key : keys) {
            String qk = Pattern.quote(key);
            // key=value or key: value (up to next whitespace)
            result = result.replaceAll("(?i)\\b" + qk + "\\s*[=:]\\s*\\S+", key + "=***");
            // key='value' or key="value"
            result = result.replaceAll("(?i)\\b" + qk + "\\s*[=:]\\s*['\"][^'\"]*['\"]", key + "=***");
        }
        return result;
    }

    private String applyStrategies(String input) {
        String result = input;
        for (MaskingStrategy strategy : maskingStrategies) {
            result = strategy.apply(result);
        }
        return result;
    }

    // ── Argument serialization ────────────────────────────────────────────────

    /**
     * Converts a method argument to its string representation, correctly handling
     * arrays and collections that {@link String#valueOf} does not serialize properly.
     */
    String toStringArg(Object arg) {
        if (arg == null)               return "null";
        if (arg instanceof Object[]  a) return Arrays.deepToString(a);
        if (arg instanceof int[]     a) return Arrays.toString(a);
        if (arg instanceof long[]    a) return Arrays.toString(a);
        if (arg instanceof double[]  a) return Arrays.toString(a);
        if (arg instanceof float[]   a) return Arrays.toString(a);
        if (arg instanceof boolean[] a) return Arrays.toString(a);
        if (arg instanceof byte[]    a) return Arrays.toString(a);
        if (arg instanceof char[]    a) return Arrays.toString(a);
        if (arg instanceof short[]   a) return Arrays.toString(a);
        if (arg instanceof Collection<?>) return arg.toString();
        if (arg instanceof Map<?, ?>)     return arg.toString();
        return String.valueOf(arg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private List<String> effectiveKeys(List<String> excludeFromMask) {
        return loggerProperties.getSensitiveKeys().stream()
                .filter(k -> !excludeFromMask.contains(k))
                .toList();
    }
}
