package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.SupervisedServiceTest;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.test.factory.AbstractCacheObjectFactory;
import cern.c2mon.server.cache.test.factory.ProcessCacheObjectFactory;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import lombok.Getter;

import javax.inject.Inject;

public class ProcessSupervisedServiceTest extends SupervisedServiceTest<Process, ProcessCacheObject> {

  @Inject
  private C2monCache<Process> equipmentCacheRef;

  @Inject
  @Getter
  private ProcessService supervisedService;

  @Override
  protected C2monCache<Process> initCache() {
    return equipmentCacheRef;
  }

  @Override
  protected AbstractCacheObjectFactory<ProcessCacheObject> initFactory() {
    return new ProcessCacheObjectFactory();
  }
}
