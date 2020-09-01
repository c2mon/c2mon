package cern.c2mon.daq.common.timer;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.config.DaqProperties;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;

/**
 * @author Franz Ritter
 */
@Component
@Slf4j
public class FreshnessMonitor {

  private final DaqProperties properties;
  private final Map<Long, Runnable> freshnessTasks = new HashMap<>();
  private IEquipmentMessageSender equipmentMessageSender;
  private final ScheduledThreadPoolExecutor timer = new ScheduledThreadPoolExecutor(2);

  @Autowired
  public FreshnessMonitor(DaqProperties properties) {
    this.properties = properties;
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

      Double freshnessTolerance = properties.getFreshnessTolerance();
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
