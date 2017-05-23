package cern.c2mon.server.jcacheref;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Szymon Halastra
 */

@Configuration
@EnableConfigurationProperties(C2monCacheProperties.class)
@ComponentScan("cern.c2mon.server.jcacheref")
public class C2monCacheModule {

  @Autowired
  C2monCacheProperties properties;
}
