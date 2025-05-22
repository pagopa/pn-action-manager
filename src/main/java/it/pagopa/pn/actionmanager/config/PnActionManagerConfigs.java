package it.pagopa.pn.actionmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
@ConfigurationProperties(prefix = "pn.action-manager")
@Data
@Import({SharedAutoConfiguration.class})
public class PnActionManagerConfigs {
}
