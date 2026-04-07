package br.com.gbs.aspecta.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot AutoConfiguration entry point for the Aspecta library.
 * <p>
 * Registers all Aspecta beans automatically without requiring the consuming
 * application to add {@code @ComponentScan("br.com.gbs.aspecta")}.
 */
@AutoConfiguration
@ComponentScan("br.com.gbs.aspecta")
public class AspectaAutoConfiguration {
}
