package cern.c2mon.server.cache.loader.loaders;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.dbaccess.config.CacheDbAccessModule;
import cern.c2mon.server.cache.loader.AlarmLoaderDAO;
import cern.c2mon.server.cache.loader.common.BatchCacheLoader;
import cern.c2mon.server.cache.loader.config.CacheLoaderModuleRef;
import cern.c2mon.server.cache.loader.config.CacheLoaderProperties;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.config.CommonModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import static org.junit.Assert.assertNotNull;

/**
 * @author Szymon Halastra
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
  CommonModule.class,
  CacheLoaderModuleRef.class,
  CacheDbAccessModule.class
}, loader = AnnotationConfigContextLoader.class)
public class BatchCacheLoaderTest {

  private C2monCache<Alarm> alarmCacheRef;

  @Autowired
  private ThreadPoolTaskExecutor cacheLoaderTaskExecutor;

  @Autowired
  private AlarmLoaderDAO alarmLoaderDAO;

  @Autowired
  private CacheLoaderProperties properties;

  private BatchCacheLoader<Alarm> batchCacheLoader;

  @Test
  public void preloadCacheFromDb() {
    batchCacheLoader = new BatchCacheLoader<>(cacheLoaderTaskExecutor, alarmCacheRef, alarmLoaderDAO,
      properties.getBatchSize(), "AlarmLoader-");
    assertNotNull("Cache loader should not be null", batchCacheLoader);
  }
}
