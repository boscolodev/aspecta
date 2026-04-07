package br.com.gbs.aspecta.logger.service;

import br.com.gbs.aspecta.logger.interfaces.I18nLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class I18NLoggerService implements I18nLogger {

    private final MessageSource messageSource;

    public String getMessage(String key, Object[] args, Locale locale) {
        return messageSource.getMessage(key, args, locale);
    }
}
