package cern.c2mon.cache.impl.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan("cern.c2mon.cache.impl")
@ImportResource("classpath:ignite-config.xml")
public class C2monIgniteConfiguration {
}
