package cern.c2mon.cache.api.listener;

import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.cache.api.listener.impl.MultiThreadListener;
import cern.c2mon.server.common.equipment.Equipment;

public class MultiThreadCacheListenerTest extends AbstractCacheListenerTest {
  @Override
  AbstractCacheListener<Equipment> generateListener() {
    return new MultiThreadListener<>(8, listenerAction);
  }

  @Override
  AbstractCacheListener<Equipment> generateMutatingListener() {
    return new MultiThreadListener<>(8, mutatingListenerAction);
  }
}
