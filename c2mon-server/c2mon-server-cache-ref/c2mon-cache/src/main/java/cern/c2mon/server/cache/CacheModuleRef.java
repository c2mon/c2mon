package cern.c2mon.server.cache;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
@ComponentScan(basePackages = {"cern.c2mon.server.cache", "cern.c2mon.cache.api"})
public class CacheModuleRef {

}
