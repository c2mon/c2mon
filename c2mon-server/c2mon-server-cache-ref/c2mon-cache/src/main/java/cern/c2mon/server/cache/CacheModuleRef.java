package cern.c2mon.server.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Configuration
@ComponentScan("cern.c2mon.server.cache")
public class CacheModuleRef {

//  @Bean(name = "cachingProvider")
//  public C2monCachingProvider getCachingProvider() {
//    return new C2monCachingProvider();
//  }
}
