package cern.c2mon.server.cache.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Justin Lewis Salmon
 */
@Data
@ConfigurationProperties(prefix = "c2mon.server.cache")
public class CacheProperties {

  /**
   * C2MON server cluster cache mode.
   *
   * Available options:
   *
   * multi: Multi-server clustered mode. Requires ehcache-ee.jar and
   * terracotta-toolkit-runtime.jar, as well as a running Terracotta instance
   * that has to be specified at C2MON startup with
   * -Dterracotta.config.location="<terracotta-main>:<port>,<terracotta-mirror>:<port>"
   *
   * single: Standalone mode. Requires ehcache-ee.jar. Supports Ehcache fast
   * cache loading, when skipPreloading=true
   *
   * single-nonpersitent: Standalone non-persistent mode. Requires
   * ehcache-core.jar. Does not support Ehcache fast cache loading.
   */
  private String mode = "single-nonpersistent";

  /**
   * Enable/disable preloading of the cache from the database at startup. This
   * flag is only relevant in "single" mode for fast cache loading from Ehcache
   * persistence file
   */
  private boolean skipPreloading = false;

  /**
   * How long (in ms) the buffered cache listener should sleep between pulls
   */
  private int bufferedListenerPullFrequency = 10000;
}
