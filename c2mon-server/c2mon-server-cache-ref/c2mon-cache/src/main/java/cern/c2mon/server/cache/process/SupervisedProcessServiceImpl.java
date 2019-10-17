package cern.c2mon.server.cache.process;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.supervision.SupervisedServiceImpl;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

class SupervisedProcessServiceImpl extends SupervisedServiceImpl<Process> {

  SupervisedProcessServiceImpl(C2monCache<Process> c2monCache, AliveTimerService aliveTimerService) {
    super(SupervisionConstants.SupervisionEntity.PROCESS, c2monCache, aliveTimerService);
  }
}
