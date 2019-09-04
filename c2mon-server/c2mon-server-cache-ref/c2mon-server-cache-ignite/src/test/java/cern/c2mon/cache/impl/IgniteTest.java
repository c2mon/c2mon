package cern.c2mon.cache.impl;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.junit.Test;

import static org.junit.Assert.fail;

public class IgniteTest {

  // We use a lot of try-with-resources because Ignite implements AutoCloseable and it can be nicely shutdown when done

  @Test
  public void configAfterStart() {
    try (Ignite ignite = Ignition.start()) {
      ignite.addCacheConfiguration(new DefaultIgniteCacheConfiguration("testCache"));
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

}
