package br.com.gbs.aspecta.logger.interfaces;

import java.util.Locale;

public interface I18nLogger {
    String getMessage(String key, Object[] args, Locale locale);
}
