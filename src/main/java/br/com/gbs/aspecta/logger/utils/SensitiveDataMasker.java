package br.com.gbs.aspecta.logger.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SensitiveDataMasker {

    private static final List<String> sensitiveKeys = List.of("password", "senha", "cpf", "cnpj", "token");

    public static String sanitizeArgs(Object[] args) {
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