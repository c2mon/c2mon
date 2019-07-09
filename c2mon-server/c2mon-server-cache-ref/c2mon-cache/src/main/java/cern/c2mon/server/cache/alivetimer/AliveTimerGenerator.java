package cern.c2mon.server.cache.alivetimer;

import org.springframework.stereotype.Service;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alive.AliveTimer;
import cern.c2mon.server.common.alive.AliveTimerCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.subequipment.SubEquipment;

/**
 * @author Szymon Halastra
 */
@Service
public class AliveTimerGenerator {

  private C2monCache<AliveTimer> aliveTimerCacheRef;

//  @Autowired
//  public AliveTimerGenerator(final C2monCache<Long, AliveTimer> aliveTimerCacheRef) {
//    this.aliveTimerCacheRef = aliveTimerCacheRef;
//  }

  public void generate(Equipment equipment) {
    AliveTimer aliveTimer = new AliveTimerCacheObject(equipment.getAliveTagId(), equipment.getId(), equipment.getName(),
            equipment.getStateTagId(), AliveTimer.ALIVE_TYPE_EQUIPMENT, equipment.getAliveInterval());
    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);
  }

  public void generate(SubEquipment subEquipment) {
    AliveTimer aliveTimer = new AliveTimerCacheObject(subEquipment.getAliveTagId(), subEquipment.getId(), subEquipment.getName(),
            subEquipment.getStateTagId(), AliveTimer.ALIVE_TYPE_SUBEQUIPMENT, subEquipment.getAliveInterval());
    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);
  }

  public void generate(Process process) {
    AliveTimer aliveTimer = new AliveTimerCacheObject(process.getAliveTagId(), process.getId(), process.getName(),
            process.getStateTagId(), AliveTimer.ALIVE_TYPE_PROCESS, process.getAliveInterval());
    aliveTimerCacheRef.put(aliveTimer.getId(), aliveTimer);
  }
}
