package it.pagopa.pn.actionmanager;

import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PnActionManagerApplication {

    public static void main(String[] args) {
        buildSpringApplicationWithListener().run(args);
    }
    static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(PnActionManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }
}