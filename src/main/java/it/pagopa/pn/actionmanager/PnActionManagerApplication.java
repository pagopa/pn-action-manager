package it.pagopa.pn.actionmanager;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static it.pagopa.pn.actionmanager.utils.SpringApplicationUtils.buildSpringApplicationWithListener;

@SpringBootApplication
public class PnActionManagerApplication {


    public static void main(String[] args) {
        buildSpringApplicationWithListener().run(args);
    }

}