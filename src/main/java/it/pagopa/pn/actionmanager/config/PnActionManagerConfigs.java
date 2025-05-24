package it.pagopa.pn.actionmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import jakarta.annotation.PostConstruct;

@Configuration
@ConfigurationProperties(prefix = "pn.action-manager")
@Data
@Import({SharedAutoConfiguration.class})
@Slf4j
public class PnActionManagerConfigs {

    private ActionDao actionDao;

    private FutureActionDao futureActionDao;

    private String actionTtlDays;

    private int detailsMaxSizeBytes;

    private int detailsMaxDepth;

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
        log.info("PnActionManagerConfigs: {}", this);
    }
}
