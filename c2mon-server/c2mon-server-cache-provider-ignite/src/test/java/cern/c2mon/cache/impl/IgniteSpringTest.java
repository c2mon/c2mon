package cern.c2mon.cache.impl;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.factory.AbstractCacheFactory;
import cern.c2mon.cache.impl.configuration.C2monIgniteConfiguration;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.inject.Inject;

import static org.junit.Assert.assertFalse;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = C2monIgniteConfiguration.class)
public class IgniteSpringTest {

  @Inject
  private AbstractCacheFactory cacheFactory;

  @Inject
  private C2monCache<Alarm> customCache;

//  @Test
//  @Ignore
//  public void createCache() {
//    C2monCache<Alarm> alarmCache = cacheFactory.createCache("alarm", Alarm.class);
//
//    assertNotNull(alarmCache);
//  }

  @Test
  public void transactionManager() {
    C2monCache<Alarm> alarmCache = customCache;
    alarmCache.init();
    Alarm sample = new AlarmCacheObject(1L);

    alarmCache.executeTransaction(() -> {
      alarmCache.put(sample.getId(), sample);

      TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
    });

    assertFalse(alarmCache.containsKey(sample.getId()));
  }
}
