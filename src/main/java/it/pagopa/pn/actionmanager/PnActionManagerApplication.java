package it.pagopa.pn.actionmanager;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;


import ch.qos.logback.classic.joran.JoranConfigurator;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.URL;

import static it.pagopa.pn.actionmanager.utils.SpringApplicationUtils.buildSpringApplicationWithListener;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import org.slf4j.LoggerFactory;
import java.net.URL;
@SpringBootApplication
public class PnActionManagerApplication {


    public static void main(String[] args) {


        URL url = PnActionManagerApplication.class.getClassLoader().getResource("logback-base.xml");
        System.out.println("!!!!!**************************************************************logback-base.xml: " + url);

        buildSpringApplicationWithListener().run(args);

    }

}