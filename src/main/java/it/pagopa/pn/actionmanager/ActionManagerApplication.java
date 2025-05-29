package it.pagopa.pn.actionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ActionManagerApplication {

    public static void main(String[] args) {
        System.setProperty("io.netty.noUnsafe", "true");

        SpringApplication.run(ActionManagerApplication.class, args);
    }
}