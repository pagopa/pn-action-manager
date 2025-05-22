package it.pagopa.pn.actionmanager;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class PnActionManagerApplication {


    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PnActionManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        app.run(args);
    }
}