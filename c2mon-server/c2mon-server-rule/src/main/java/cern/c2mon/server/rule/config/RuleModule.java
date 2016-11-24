package cern.c2mon.server.rule.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableConfigurationProperties(RuleProperties.class)
@ComponentScan("cern.c2mon.server.rule")
public class RuleModule {}
