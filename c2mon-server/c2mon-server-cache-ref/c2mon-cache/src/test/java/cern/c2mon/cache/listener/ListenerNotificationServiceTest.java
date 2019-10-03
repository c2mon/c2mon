package cern.c2mon.cache.listener;

import cern.c2mon.cache.AbstractCacheTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.listener.CacheListener;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Repeat;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ListenerNotificationServiceTest extends AbstractCacheTest<Process> {

  private static final List<Map.Entry<Object, Boolean>> results = new ArrayList<>();
  private static final CacheListener<Process> cacheListener = new CacheListener<Process>() {
    @Override
    public void notifyElementUpdated(Process cacheable) {
      results.add(new SimpleEntry<>(this, true));
    }

    @Override
    public void confirmStatus(Process cacheable) {
      results.add(new SimpleEntry<>(this, true));
    }
  };

  @Autowired
  private C2monCache<Process> cacheRef;
  private Process sampleItem = new ProcessCacheObject(1L);
  private Process sampleItem2 = new ProcessCacheObject(1L, "Abc", 1L, 1000, 1000);

  @Override
  protected C2monCache<Process> initCache() {
    cacheRef.registerListener(cacheListener);
    return cacheRef;
  }

  @Before
  public void resetResults() {
    results.clear();
  }

  @Test
  @Repeat(value = 100)
  public void notifyListenersOfUpdate() {
    cacheRef.put(1L, sampleItem);
    cacheRef.put(1L, sampleItem2);

    Assert.assertEquals(2, results.size());
    results.forEach(entry -> assertTrue(entry.getValue()));
  }

  @Test
  public void notifyListenersOfSupervisionChange() {

  }

  @Test
  public void notifyListenerStatusConfirmation() {

  }

  @Test
  public void multipleListenersStackUpdates() {
//    cacheRef.registerListener(cacheListener);
//    cacheRef.registerListener(cacheListener);
//    cacheRef.registerListener(cacheListener);
//
//    cacheRef.put(1L, sampleItem);
//    cacheRef.put(1L, sampleItem2);
//
//    Assert.assertEquals(3, results.size());
//    results.forEach(entry -> assertTrue(entry.getValue()));

  }

  @Test
  public void listenersReceiveUpdatedItem() {

  }

  @Test
  public void registeringListenerAgainThrows() {

  }
}
