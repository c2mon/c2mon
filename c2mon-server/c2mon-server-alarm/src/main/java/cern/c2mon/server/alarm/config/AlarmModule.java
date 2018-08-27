package cern.c2mon.server.alarm.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableConfigurationProperties(OscillationProperties.class)
@ComponentScan("cern.c2mon.server.alarm")
public class AlarmModule {}
