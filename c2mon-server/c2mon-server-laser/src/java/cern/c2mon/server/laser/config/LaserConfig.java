package cern.c2mon.server.laser.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@ImportResource("classpath:cern/c2mon/server/laser/config/server-laser.xml")
public class LaserConfig {}
