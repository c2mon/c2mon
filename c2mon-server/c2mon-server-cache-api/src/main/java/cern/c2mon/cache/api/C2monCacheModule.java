package cern.c2mon.cache.api;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(C2monCacheProperties.class)
@ComponentScan("cern.c2mon.cache.api")
public class C2monCacheModule {

}
