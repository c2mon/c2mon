package cern.c2mon.server.ehcache.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
public class CacheConfiguration  {

    @Autowired
    private ConfigurableEnvironment env;

    @Autowired
    private CacheProperties cacheProperties;


}
