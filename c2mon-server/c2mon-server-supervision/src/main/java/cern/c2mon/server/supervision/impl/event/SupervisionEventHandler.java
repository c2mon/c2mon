package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;

@Slf4j
abstract class SupervisionEventHandler<T extends Supervised> {

  // You can technically modify this, but don't - the self registration in Ctor is all that's needed
  @Getter
  private static Map<Class<? extends Supervised>, SupervisionEventHandler<? extends Supervised>> eventHandlers = Collections.emptyMap();
  protected SupervisedCacheService<T> service;
  protected C2monCache<T> cache;

  SupervisionEventHandler(Class<T> clazz, SupervisedCacheService<T> service) {
    this.service = service;
    this.cache = service.getCache();
    eventHandlers.putIfAbsent(clazz, this);
  }

  public void onUp(Long id, Timestamp timestamp, String message) {
    try {
      service.resume(id, timestamp, message);
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Cannot locate the Supervised object in the cache - unable to update it.", cacheEx);
    }
  }

  public void onAliveTimerDown(long supervisedId) {
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    onDown(supervisedId, timestamp, "Alive timer has expired for Supervised with id: " + supervisedId);
  }

  /**
   * Called when a DAQ alive timer expires.
   * <p>
   * The onDown() method sets the value of the state tag associated with
   * the supervised object to "DOWN". If the value of the state tag is already "DOWN",
   * no further action is taken.
   * <p>
   * Call within block synchronized on this Supervised obj.
   */
  public void onDown(Long id, Timestamp timestamp, String message) {
    service.suspend(id, timestamp, message);

    Long stateTagId = supervised.getStateTagId();

//  TODO (Alex) This should also propagate to CommFaultTags
//  TODO (Alex) Review and execute this flow. See also the onUp()

//    try {
//      Long stateTagId = supervised.getStateTagId();
//      if (stateTagId == null) {
//        log.error("State tag Id is set to null for Process + " + processCopy.getId() + " - unable to update it.");
//      } else {
//      if (!filterOut) { // Done as a DatatagCacheFlow
//        tag = dataTagCache.get(...)
//        tag.getDataTagQuality().validate(); // Here we want the opposite for onDown?
//        AbstractTagObjectFacade.updateValue(dataTag, value, valueDesc);
//        setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp);
//      }
//    } catch (CacheElementNotFoundException cacheEx) {
//      log.error("Cannot locate the Process State tag in the cache - unable to update it.", cacheEx);
//    }
  }

  /**
   * Manually set the value of a CommFaultTag. Used to update a CommFaultTag
   * when an AliveTimer expires, in order to keep the two tags consistent with
   * each other.
   *
   * @param commFaultTagId the ID of the tag to set
   * @param value          the value of the tag
   * @param message        the value description to set for the tag
   */
  protected void setCommFaultTag(final Long commFaultTagId, final boolean value, final String message, final Timestamp timestamp) {
    try {
      controlTagFacade.updateAndValidate(commFaultTagId, value, message, timestamp);
    } catch (CacheElementNotFoundException e) {
      log.error("Could not locate CommFaultTag (id: " + commFaultTagId + ") in cache");
    }
  }
}
