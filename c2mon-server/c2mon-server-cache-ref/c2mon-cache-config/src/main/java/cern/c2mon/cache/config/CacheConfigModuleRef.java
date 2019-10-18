package cern.c2mon.cache.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
@ComponentScan(basePackages = {"cern.c2mon.cache.config", "cern.c2mon.cache.api"})
public class CacheConfigModuleRef {

}
