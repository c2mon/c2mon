package cern.c2mon.cache.impl;

import cern.c2mon.server.common.AbstractCacheableImpl;
import lombok.EqualsAndHashCode;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.junit.Test;

import javax.cache.Cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class IgniteTest {

  // We use a lot of try-with-resources because Ignite implements AutoCloseable and it can be nicely shutdown when done

  @Test
  public void configAfterStart() {
    try (Ignite ignite = Ignition.start()) {
      ignite.addCacheConfiguration(IgniteFactory.defaultIgniteCacheConfiguration("testCache"));
    }
  }

  @Test
  public void doubleStartFails() {
    try (Ignite ignored = Ignition.start()) {
      try {
        Ignition.start();
        fail("Starting ignite instance twice should not be allowed");
      } catch (IgniteException e) {
        // Do nothing
      }
    }
  }

  @Test
  public void mutateObject() {
    try (Ignite ignite = Ignition.start()) {
      Cache<Long, DeepClass> cache = ignite.createCache(IgniteFactory.defaultIgniteCacheConfiguration("testCache"));
      cache.put(1L, new DeepClass(1, "Instance 1"));
      cache.get(1L).name = "Instance 2";
      assertEquals(cache.get(1L).name, "Instance 1");
      cache.replace(1L, new DeepClass(1, "Instance 2"));
      assertEquals(cache.get(1L).name, "Instance 2");
    }
  }

  @EqualsAndHashCode(callSuper = true)
  private class DeepClass extends AbstractCacheableImpl {
    long id;
    String name;

    public DeepClass(long id, String name) {
      super(id);
      this.name = name;
    }
  }
}
