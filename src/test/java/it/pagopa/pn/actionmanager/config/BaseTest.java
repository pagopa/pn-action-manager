package it.pagopa.pn.actionmanager.config;

import it.pagopa.pn.actionmanager.LocalStackTestConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;


@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseTest {


    @Slf4j
    @SpringBootTest
    @ActiveProfiles("test")
    @Import(LocalStackTestConfig.class)
    public static class WithLocalStack {

    }

}
