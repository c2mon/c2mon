package cern.c2mon.cache.api.listener;

import cern.c2mon.cache.api.listener.impl.AbstractCacheListener;
import cern.c2mon.cache.api.listener.impl.SingleThreadListener;
import cern.c2mon.server.common.equipment.Equipment;

public class SingleThreadCacheListenerTest extends AbstractCacheListenerTest {
  @Override
  AbstractCacheListener<Equipment> generateListener() {
    return new SingleThreadListener<>(listenerAction);
  }

  @Override
  AbstractCacheListener<Equipment> generateMutatingListener() {
    return new SingleThreadListener<>(mutatingListenerAction);
  }
}
