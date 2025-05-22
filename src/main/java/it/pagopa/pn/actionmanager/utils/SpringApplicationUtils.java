package it.pagopa.pn.actionmanager.utils;

import it.pagopa.pn.actionmanager.PnActionManagerApplication;
import it.pagopa.pn.commons.configs.listeners.TaskIdApplicationListener;

import org.springframework.boot.SpringApplication;

public class SpringApplicationUtils {

    public static SpringApplication buildSpringApplicationWithListener() {
        SpringApplication app = new SpringApplication(PnActionManagerApplication.class);
        app.addListeners(new TaskIdApplicationListener());
        return app;
    }
}
