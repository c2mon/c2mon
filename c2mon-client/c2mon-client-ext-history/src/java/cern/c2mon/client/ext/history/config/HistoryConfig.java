package cern.c2mon.client.ext.history.config;

import org.springframework.context.annotation.*;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@PropertySource("classpath:history.properties")
@ImportResource("classpath:spring-history.xml")
public class HistoryConfig {}
