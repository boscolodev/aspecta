package br.com.gbs.aspecta.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Logback appender that stores log events in memory for assertion in tests.
 *
 * <pre>{@code
 * Logger logger = (Logger) LoggerFactory.getLogger(MyClass.class);
 * MemoryAppender appender = new MemoryAppender();
 * appender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
 * logger.addAppender(appender);
 * appender.start();
 *
 * // exercise the code under test ...
 *
 * assertThat(appender.contains("expected substring", Level.INFO)).isTrue();
 * appender.detachFrom(logger);
 * }</pre>
 */
public class MemoryAppender extends AppenderBase<ILoggingEvent> {

    private final List<ILoggingEvent> events = new CopyOnWriteArrayList<>();

    @Override
    protected void append(ILoggingEvent event) {
        events.add(event);
    }

    public List<ILoggingEvent> getEvents() {
        return List.copyOf(events);
    }

    public void reset() {
        events.clear();
    }

    public boolean contains(String substring, ch.qos.logback.classic.Level level) {
        return events.stream().anyMatch(e ->
                e.getLevel().equals(level) &&
                e.getFormattedMessage().contains(substring));
    }

    public boolean containsMessage(String substring) {
        return events.stream().anyMatch(e ->
                e.getFormattedMessage().contains(substring));
    }

    public void detachFrom(ch.qos.logback.classic.Logger logger) {
        logger.detachAppender(this);
        stop();
    }
}
