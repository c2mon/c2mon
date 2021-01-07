package cern.c2mon.cache.actions;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class used to component scan the Cache Actions module
 * @author Szymon Halastra
 */
@Configuration
@ComponentScan(basePackages = {"cern.c2mon.cache.actions"})
public class CacheActionsModuleRef {

}
