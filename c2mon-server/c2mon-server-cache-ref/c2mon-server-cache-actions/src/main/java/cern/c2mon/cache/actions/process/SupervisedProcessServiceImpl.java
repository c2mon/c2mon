package cern.c2mon.cache.actions.process;

import cern.c2mon.cache.actions.alive.AliveTagService;
import cern.c2mon.cache.actions.datatag.DataTagService;
import cern.c2mon.cache.actions.supervision.AbstractSupervisedService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.common.supervision.SupervisionEntity;

class SupervisedProcessServiceImpl extends AbstractSupervisedService<Process> {

  SupervisedProcessServiceImpl(final C2monCache<Process> c2monCache,
                               final AliveTagService aliveTimerService,
                               final DataTagService dataTagService) {
    super(c2monCache, SupervisionEntity.PROCESS, aliveTimerService, dataTagService);
  }
}
