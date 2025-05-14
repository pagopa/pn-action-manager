package it.pagopa.pn.actionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ActionManagerApplication {


    public static void main(String[] args) {
        SpringApplication.run(ActionManagerApplication.class, args);
    }
}