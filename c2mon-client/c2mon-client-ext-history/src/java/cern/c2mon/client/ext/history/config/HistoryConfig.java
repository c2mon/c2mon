package cern.c2mon.client.ext.history.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@PropertySource("classpath:history.properties")
@ImportResource("classpath:spring-history.xml")
public class HistoryConfig {}
