package br.com.gbs.aspecta.logger.utils;

import br.com.gbs.aspecta.logger.configurations.LoggerProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SensitiveDataMasker {

    private final LoggerProperties loggerProperties;

    public String sanitizeArgs(Object[] args, boolean applyMasking) {
        if (!applyMasking) {
            return Arrays.stream(args)
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
        }

        List<String> sensitiveKeys = loggerProperties.getSensitiveKeys();

        return Arrays.stream(args)
                .map(arg -> {
                    String json = String.valueOf(arg);
                    for (String key : sensitiveKeys) {
                        json = json.replaceAll("(?i)(\"" + key + "\"\\s*:\\s*\")[^\"]+\"", "$1***\"");
                    }
                    return json;
                })
                .collect(Collectors.joining(", "));
    }
}
