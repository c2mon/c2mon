package cern.c2mon.server.cache.alivetimer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;

/**
 * @author Szymon Halastra
 */
@Service
public class AliveTimerGenerator {

  private C2monCache<Long, AliveTimer> aliveTimerCacheRef;

  @Autowired
  public AliveTimerGenerator(final C2monCache<Long, AliveTimer> aliveTimerCacheRef) {
    this.aliveTimerCacheRef = aliveTimerCacheRef;
  }

  public void generateFromEquipment(AbstractEquipment abstractEquipment) {
    String type;
    if (abstractEquipment instanceof Equipment) {
      type = AliveTimer.ALIVE_TYPE_EQUIPMENT;
    }
    else {
      type = AliveTimer.ALIVE_TYPE_SUBEQUIPMENT;
    }
    AliveTimer aliveTimer = new AliveTimerCacheObject(abstractEquipment.getAliveTagId(), abstractEquipment.getId(), abstractEquipment.getName(),
            abstractEquipment.getStateTagId(), type, abstractEquipment.getAliveInterval());
    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);
  }

  public void generateFromProcess(Process process) {
    AliveTimer aliveTimer = new AliveTimerCacheObject(process.getAliveTagId(), process.getId(), process.getName(),
            process.getStateTagId(), AliveTimer.ALIVE_TYPE_PROCESS, process.getAliveInterval());
    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);
  }
}
