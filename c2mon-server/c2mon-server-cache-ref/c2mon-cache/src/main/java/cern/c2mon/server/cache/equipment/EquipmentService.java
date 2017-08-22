package cern.c2mon.server.cache.equipment;

import java.sql.Timestamp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.service.SupervisedManager;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.common.supervision.SupervisionConstants;

/**
 * @author Szymon Halastra
 */

@Slf4j
@Service
public class EquipmentService implements SupervisedManager<Equipment> {

  private final C2monCache<Long, Process> processCache;

  private final C2monCache<Long, DataTag> dataTagCache;

  private final SupervisedManager<Equipment> supervisedService;

  @Autowired
  public EquipmentService(final C2monCache<Long, Process> processCache, final C2monCache<Long, DataTag> dataTagCache,
                          final SupervisedManager<Equipment> supervisedService) {
    this.processCache = processCache;
    this.dataTagCache = dataTagCache;
    this.supervisedService = supervisedService;
  }

  @Override
  public SupervisionEvent getSupervisionStatus(Long id) {
    return supervisedService.getSupervisionStatus(id);
  }

  @Override
  public void refreshAndNotifyCurrentSupervisionStatus(Long id) {
    supervisedService.refreshAndNotifyCurrentSupervisionStatus(id);
  }

  @Override
  public void start(Long id, Timestamp timestamp) {
    supervisedService.start(id, timestamp);
  }

  @Override
  public void stop(Long id, Timestamp timestamp) {
    supervisedService.stop(id, timestamp);
  }

  @Override
  public void resume(Long id, Timestamp timestamp, String message) {
    supervisedService.resume(id, timestamp, message);
  }

  @Override
  public void suspend(Long id, Timestamp timestamp, String message) {
    supervisedService.suspend(id, timestamp, message);
  }

  @Override
  public boolean isRunning(Equipment supervised) {
    return supervisedService.isRunning(supervised);
  }

  @Override
  public boolean isRunning(Long id) {
    return supervisedService.isRunning(id);
  }

  @Override
  public boolean isUncertain(Equipment supervised) {
    return supervisedService.isUncertain(supervised);
  }

  @Override
  public void removeAliveTimer(Long id) {
    supervisedService.removeAliveTimer(id);
  }

  @Override
  public void loadAndStartAliveTag(Long supervisedId) {
    supervisedService.loadAndStartAliveTag(supervisedId);
  }

  @Override
  public void removeAliveDirectly(Long aliveId) {
    supervisedService.removeAliveTimer(aliveId);
  }

  @Override
  public SupervisionConstants.SupervisionEntity getSupervisionEntity() {
    return supervisedService.getSupervisionEntity();
  }

  @Override
  public void setSupervisionEntity(SupervisionConstants.SupervisionEntity entity) {
    supervisedService.setSupervisionEntity(entity);
  }
}
