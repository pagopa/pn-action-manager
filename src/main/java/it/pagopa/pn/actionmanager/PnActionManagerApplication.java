package it.pagopa.pn.actionmanager;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static it.pagopa.pn.actionmanager.utils.SpringApplicationUtils.buildSpringApplicationWithListener;

import java.io.InputStream;

@SpringBootApplication
public class PnActionManagerApplication {


    public static void main(String[] args) {
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("logback-base.xml");
        System.out.println("Logback config found: " + (is != null));

        buildSpringApplicationWithListener().run(args);
    }

}