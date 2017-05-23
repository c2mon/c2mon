package cern.c2mon.server.jcacheref;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Szymon Halastra
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.cache")
public class C2monCacheProperties {
}
