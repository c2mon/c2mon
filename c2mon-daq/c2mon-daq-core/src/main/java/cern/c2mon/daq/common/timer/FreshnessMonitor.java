package cern.c2mon.daq.common.timer;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.SourceDataTagQualityCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Franz Ritter
 */
@Component
@Slf4j
public class FreshnessMonitor {

  @Autowired
  private Environment environment;

  private Map<Long, Runnable> freshnessTasks;

  private IEquipmentMessageSender equipmentMessageSender;

  private ScheduledThreadPoolExecutor timer;

  public FreshnessMonitor() {
    this.timer = new ScheduledThreadPoolExecutor(2);
    this.freshnessTasks = new HashMap<>();
  }

  public void setIEquipmentMessageSender(IEquipmentMessageSender equipmentMessageSender) {
    this.equipmentMessageSender = equipmentMessageSender;
  }

  public void reset(SourceDataTag sourceDataTag) {

    if (sourceDataTag.getAddress() != null
        && sourceDataTag.getAddress().getFreshnessInterval() != null
        && sourceDataTag.getAddress().getFreshnessInterval() != 0L) {

      log.trace("Checking freshness for tag {}", sourceDataTag.getName());
      Long tagId = sourceDataTag.getId();
      Runnable task;

      if (freshnessTasks.containsKey(tagId)) {
        task = freshnessTasks.get(tagId);
        timer.remove(task);
        timer.purge();

      } else {
        task = new FreshnessTask(tagId);
        freshnessTasks.put(tagId, task);
      }

      Double freshnessTolerance = environment.getRequiredProperty("c2mon.daq.freshness.tolerance", Double.class);
      timer.schedule(task, (long) (sourceDataTag.getAddress().getFreshnessInterval() * freshnessTolerance), TimeUnit.SECONDS);
    }
  }

  public void removeDataTag(SourceDataTag sourceDataTag) {
    if (sourceDataTag.getAddress() != null
        && sourceDataTag.getAddress().getFreshnessInterval() != null
        && sourceDataTag.getAddress().getFreshnessInterval() != 0L) {

      Long tagId = sourceDataTag.getId();
      if (freshnessTasks.containsKey(tagId)) {
        log.trace("Removing freshness check for tag {}", sourceDataTag.getName());
        timer.remove(freshnessTasks.get(tagId));
        freshnessTasks.remove(tagId);
      }
    }
  }

  class FreshnessTask implements Runnable {

    private Long id;

    public FreshnessTask(Long id) {
      this.id = id;
    }

    @Override
    public void run() {
      SourceDataTagQuality tagQuality = new SourceDataTagQuality(SourceDataTagQualityCode.STALE);
      equipmentMessageSender.update(id, tagQuality);
    }
  }

}
