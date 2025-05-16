package it.pagopa.pn.actionmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.CustomLog;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;


@Configuration
@ConfigurationProperties(prefix = "pn.action-manager")
@Data
@Import({SharedAutoConfiguration.class})
public class PnActionManagerConfigs {

    private ActionDao actionDao;

    private FutureActionDao futureActionDao;

    private String actionTtlDays;

    @Data
    public static class ActionDao {
        private String tableName;
    }

    @Data
    public static class FutureActionDao {
        private String tableName;
    }

    @PostConstruct
    public void init() {
        System.out.println(this);
    }

}
