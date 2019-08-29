package cern.c2mon.server.cache.process;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedService;
import cern.c2mon.server.cache.alivetimer.AliveTimerService;
import cern.c2mon.server.cache.equipment.EquipmentService;
import cern.c2mon.server.common.config.ServerProperties;
import cern.c2mon.server.common.process.Process;
import lombok.Getter;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Szymon Halastra
 * @author Alexandros Papageorgiou Koufidis
 */
@Slf4j
@Service
public class ProcessService implements ProcessOperationService, SupervisedService<Process> {

  @Delegate(types = SupervisedProcessService.class)
  private SupervisedService<Process> supervisedService;

  @Delegate(types = ProcessOperationService.class)
  private ProcessOperationService processOperationService;

  @Getter
  private C2monCache<Process> processCacheRef;

  @Autowired
  public ProcessService(final EquipmentService equipmentService, final AliveTimerService aliveTimerService,
                        final C2monCache<Process> processCacheRef, final ServerProperties properties) {
    this.processCacheRef = processCacheRef;

    this.supervisedService = new SupervisedProcessServiceImpl(processCacheRef, aliveTimerService);
    this.processOperationService = new ProcessOperationServiceImpl(processCacheRef, equipmentService, aliveTimerService, properties);
  }

  /**
   * This interface serves only as a static type reference to {@link SupervisedService}<{@link Process}>
   * <p>
   * That is required only for the {@code @Delegate} Lombok annotation above
   *
   * @see <a href=https://projectlombok.org/features/experimental/Delegate>Lombok Delegate docs</a>
   */
  private interface SupervisedProcessService extends SupervisedService<Process> {
  }
}

