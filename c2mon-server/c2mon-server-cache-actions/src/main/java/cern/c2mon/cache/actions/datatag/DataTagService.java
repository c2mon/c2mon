package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.common.thread.Event;
import cern.c2mon.shared.common.CacheEvent;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static cern.c2mon.cache.actions.datatag.DataTagEvaluator.dataTypeMatches;

/**
 * @author Alexandros Papageorgiou
 */
@Slf4j
@Named
@Singleton
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
    return cache.query(dataTag -> dataTag.getEquipmentIds().contains(equipmentId))
      .stream().map(Tag::getId).collect(Collectors.toSet());
  }

  /**
   * Returns a collection of the ids of all DataTags registered with this
   * subequipment (not control tags).
   *
   * @param subEquipmentId of the subequipment
   * @return the ids in a collection
   */
  public Collection<Long> getDataTagIdsBySubEquipmentId(Long subEquipmentId) {
    return cache.query(dataTag -> dataTag.getSubEquipmentIds().contains(subEquipmentId))
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

  public void resetQualityToValid(Long id) {
    cache.compute(id, TagController::validate);
  }

  /**
   * Generates the DAQ configuration XML structure for a single data tag
   *
   * @param id the id of the data tag
   * @return returns the configuration XML as a String;
   * if the tag could not be located in the cache, logs an error and returns the empty String
   */
  public String generateSourceXmlById(final Long id) {
    String returnValue = "";
    try {
      DataTag dataTag = cache.get(id);
      returnValue = DataTagController.generateSourceXML(dataTag);
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("getConfigXML(): failed to retrieve data tag with id " + id + " from the cache (returning empty String config).", cacheEx);
    }
    return returnValue;
  }

  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * DataTagCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   *
   * <p>Also notifies the listeners if an update was performed.
   *
   * <p>Notice the tag is not put back in the cache here.
   *
   * @param id                 the id of a DataTag to find
   * @param sourceDataTagValue the source value received from the DAQ
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  public Event<Boolean> updateFromSource(long id, final SourceDataTagValue sourceDataTagValue) {
    return cache.executeTransaction(() -> {
      final DataTag dataTag = cache.get(id);

      if (sourceDataTagValue == null) {
        log.error("Attempting to update a dataTag with a null source value - ignoring update.");
        return new Event<>(dataTag.getCacheTimestamp().getTime(), false);
      }

      // TODO (Alex) This does not properly account for potential filterout as part of cache.put. Should it?
      Event<Boolean> returnValue = updateFromSource(dataTag, sourceDataTagValue);

      if (sourceDataTagValue.isValid()) {
        cache.putQuiet(id, dataTag);
      } else {
        cache.put(id, dataTag);
      }

      return returnValue;
    });
  }

  /**
   * Updates the tag object if the value is not filtered out. Contains the logic on when a
   * DataTagCacheObject should be updated with new values and when not (in particular
   * timestamp restrictions).
   *
   * <p>Also notifies the listeners if an update was performed.
   * <p>
   * Notice the tag is not put back in the cache here, but will be as soon as the method returns
   *
   * @param dataTag            is modified by the method
   * @param sourceDataTagValue the source value received from the DAQ
   * @return true if an update was performed (i.e. the value was not filtered out)
   */
  private Event<Boolean> updateFromSource(DataTag dataTag, final SourceDataTagValue sourceDataTagValue) {

    if (dataTypeMatches(dataTag, sourceDataTagValue)) {
      sourceDataTagValue.setValue(DataTagController.castSourceDataTagValue(dataTag, sourceDataTagValue));
    }

    DataTagController.setValidation(dataTag, sourceDataTagValue);

    return new Event<>(System.currentTimeMillis(), true);
  }

}
