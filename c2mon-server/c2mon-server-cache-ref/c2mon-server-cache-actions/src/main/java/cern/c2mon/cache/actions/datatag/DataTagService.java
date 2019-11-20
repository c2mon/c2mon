package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.AbstractCacheServiceImpl;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * @author Alexandros Papageorgiou
 */
@Slf4j
@Service
public class DataTagService extends AbstractCacheServiceImpl<DataTag> {

  @Inject
  public DataTagService(C2monCache<DataTag> cache) {
    super(cache, new DataTagC2monCacheFlow<>());
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
      if (!filterout(dataTag, )) // TODO (Alex) Also check TagService.filterout?
        dataTag.getDataTagQuality().validate();
//        setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp);
    });
  }

  /**
   * Method containing all the logic for filtering out incoming datatag updates before any updates are
   * attempted. Call within synchronized block.
   * @return true if the update should be filtered out, false if it should be kept
   */
  private boolean filterout(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {

    //NOW checked in SourceUpdateManager; source values are NOT filtered out now, but tag is invalidated (unless filtered out for timestamp
    //reasons).
//    if (sourceDataTagValue.getValue() == null) {
//      log.warn("Attempted to update DataTag " + sourceDataTagValue.getId() + " with a null value.");
//      return true;
//    }

    //set the timestamps to compare:
    //(1)if both daq timestamps are set, compare these
    //(2)if not, use the source timestamps
    //TODO change here once the source value has both DAQ and src timestamps
    Timestamp dataTagTimestamp, sourceTagTimestamp;

    if (dataTag.getDaqTimestamp() != null && sourceDataTagValue.getDaqTimestamp() != null) {

      dataTagTimestamp = dataTag.getDaqTimestamp();
      sourceTagTimestamp = sourceDataTagValue.getDaqTimestamp();

    } else if (dataTag.getSourceTimestamp() != null && sourceDataTagValue.getTimestamp() != null) {

      //only for backwards compatibility until all DAQs are sending DAQ timestamps
      dataTagTimestamp = dataTag.getSourceTimestamp();
      sourceTagTimestamp = sourceDataTagValue.getTimestamp();

    } else {

      return false; //since only server timestamp is set on dataTag, all incoming source values should be accepted
    }

    //neither timestamps should be null from here

    /*
     * Do NOT update the tag if the new timestamp is OLDER.
     * EXCEPTION:
     * If the datatag is currently marked as INACCESSIBLE, we can override the value BUT
     * the timestamp will be the current time.
     * EXCEPTION2:
     * If the user sets the forceUpdate flag, we perform the update regardless of whether all other
     * conditions are met. This flag must be used with great care.
     */
    //removed forceUpdate below
    if (sourceTagTimestamp.before(dataTagTimestamp)) {

      if (dataTag.getDataTagQuality() == null || dataTag.getDataTagQuality().isAccessible()) {

        log.debug("update() : older timestamp and not inaccessible -> reject update");
        return true;

      }
      else {
        log.debug("update() : older timestamp but tag currently inaccessible -> update with older timestamp");
      }
    }

    /*
     * If the timestamp of the new value is the same as the old timestamp, only
     * perform an update if the values are different (and valid). The values are considered
     * to be different by default if the old value is null. EXCEPTION: If the
     * user sets the forceUpdate flag, we perform the update regardless of
     * whether all other conditions are met. This flag must be used with great
     * care.
     */
    if (sourceTagTimestamp.equals(dataTagTimestamp) && dataTag.getValue() != null
      && sourceDataTagValue.getValue().equals(dataTag.getValue()) && dataTag.getDataTagQuality().isValid()
      && sourceDataTagValue.getQuality() != null  && sourceDataTagValue.getQuality().isValid()) {

      log.debug("update() : values and timestamps are equal, so nothing to update -> reject update");
      return true;
    }

    //false means allow the update to proceed
    return false;
  }
}
