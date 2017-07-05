package cern.c2mon.server.jcacheref;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.spi.CachingProvider;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
@Slf4j
public class JCacheTest extends HazelcastBaseTestingSetup {

  CachingProvider provider;

  @Before
  public void setup() {
    provider = Caching.getCachingProvider();
  }

  @Test
  public void checkProvider() {
    assertNotNull(provider);
  }

  @Test
  public void checkCacheManager() {
    CacheManager cacheManager = provider.getCacheManager();

    assertNotNull(cacheManager);
  }
}
