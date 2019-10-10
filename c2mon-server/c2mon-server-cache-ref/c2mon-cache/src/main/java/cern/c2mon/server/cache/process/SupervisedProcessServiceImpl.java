package cern.c2mon.server.cache.process;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.supervision.SupervisedServiceImpl;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

import java.sql.Timestamp;

class SupervisedProcessServiceImpl extends SupervisedServiceImpl<Process> {

  SupervisedProcessServiceImpl(C2monCache<Process> c2monCache, AliveTimerService aliveTimerService) {
    super(c2monCache, aliveTimerService);
  }

  @Override
  public void stop(Process process, Timestamp timestamp) {
    cacheRef.executeTransaction(() -> {
      ProcessCacheObject processCacheObject = (ProcessCacheObject) process;
      processCacheObject.setCurrentHost(null);
      processCacheObject.setStartupTime(null);
      processCacheObject.setRequiresReboot(Boolean.FALSE);
      processCacheObject.setProcessPIK(null);
      processCacheObject.setLocalConfig(null);
      // What is this? It looks faulty
      this.stop(process, timestamp);
    });
  }

  @Override
  public SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return SupervisionConstants.SupervisionEntity.PROCESS;
  }
}
