package it.pagopa.pn.actionmanager.config;

import it.pagopa.pn.commons.conf.SharedAutoConfiguration;
import lombok.CustomLog;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@ConfigurationProperties(prefix = "pn.action-manager")
@Data
@Import({SharedAutoConfiguration.class})
@CustomLog
public class PnActionManagerConfigs {
}
