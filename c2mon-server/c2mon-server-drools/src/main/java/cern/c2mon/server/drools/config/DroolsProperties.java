package cern.c2mon.server.drools.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "c2mon.server.drools")
public class DroolsProperties {

}