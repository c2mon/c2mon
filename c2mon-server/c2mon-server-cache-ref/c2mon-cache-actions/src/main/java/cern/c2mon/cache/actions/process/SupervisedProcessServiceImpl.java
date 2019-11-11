package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.actions.alivetimer.AliveTimerService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

// TODO Is this used for anything?
class SupervisedProcessServiceImpl extends AbstractSupervisedService<Process> {

  SupervisedProcessServiceImpl(C2monCache<Process> c2monCache, AliveTimerService aliveTimerService) {
    super(c2monCache, SupervisionConstants.SupervisionEntity.PROCESS, aliveTimerService);
  }
}
