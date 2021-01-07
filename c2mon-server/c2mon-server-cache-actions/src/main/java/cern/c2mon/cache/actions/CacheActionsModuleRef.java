package cern.c2mon.cache.actions;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

/**
 * Configuration class used to component scan the cache actions base package
 */
@Configuration
@ComponentScan(basePackages = {"cern.c2mon.cache.actions"})
public class CacheActionsModuleRef {

}
