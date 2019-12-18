package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * @author Alexandros Papageorgiou
 */
@Slf4j
@Service
public class DataTagService extends AbstractCacheServiceImpl<DataTag> {

  private final C2monCache<Alarm> alarmCache;
  private final C2monCache<RuleTag> ruleTagCache;

  @Inject
  public DataTagService(C2monCache<DataTag> cache, C2monCache<Alarm> alarmCache, C2monCache<RuleTag> ruleTagCache) {
    super(cache, new DataTagCacheFlow());
    this.alarmCache = alarmCache;
    this.ruleTagCache = ruleTagCache;
  }

  @PostConstruct
  public void init() {
    getCache().getCacheListenerManager().registerListener(dataTag -> {
      alarmCache.removeAll(new HashSet<>(dataTag.getAlarmIds()));
      ruleTagCache.removeAll(new HashSet<>(dataTag.getRuleIds()));
    }, CacheEvent.REMOVED);
  }

  /**
   * Returns a collection of the ids of all DataTags
   * registered with this equipment
   */
  public Collection<Long> getDataTagIdsByEquipmentId(Long equipmentId) {
    return cache.query(dataTag -> dataTag.getEquipmentId().equals(equipmentId))
      .stream().map(Tag::getId).collect(Collectors.toSet());
  }

  /**
   * Returns a collection of the ids of all DataTags
   * registered with this DAQ
   */
  public Collection<Long> getDataTagIdsByProcessId(Long processId) {
    return cache.query(dataTag -> dataTag.getProcessId().equals(processId))
      .stream().map(DataTag::getId).collect(Collectors.toSet());
  }

  public void resetQualityToValid(Long id){
    cache.compute(id, dataTag -> {
        dataTag.getDataTagQuality().validate();
//        setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp); TODO (Alex) Turn this on
    });
  }

  /**
   * Generates a {@link SourceDataTag} object from the given data tag
   * @param dataTag The data tag which shall be converted
   * @return The resulting source data tag
   */
  public final SourceDataTag generateSourceDataTag(final DataTag dataTag) {
    SourceDataTag sourceDataTag = new SourceDataTag(dataTag.getId(), dataTag.getName(), false);
    sourceDataTag.setDataType(dataTag.getDataType());
    sourceDataTag.setMode(dataTag.getMode());
    sourceDataTag.setMinValue((Number) dataTag.getMinValue());
    sourceDataTag.setMaxValue((Number) dataTag.getMaxValue());
    if (dataTag.getAddress() != null) {
      sourceDataTag.setAddress(dataTag.getAddress());
    }
    return sourceDataTag;
  }
}
