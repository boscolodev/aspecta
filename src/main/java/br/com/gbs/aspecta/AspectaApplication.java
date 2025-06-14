package br.com.gbs.aspecta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
public class AspectaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AspectaApplication.class, args);
    }

}
