package cern.c2mon.client.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

/**
 * @author Justin Lewis Salmon
 */
@Configuration
@EnableConfigurationProperties(C2monClientProperties.class)
@ComponentScan({
    "cern.c2mon.client.core",
    "cern.c2mon.client.ext"
})
public class C2monAutoConfiguration {}
