package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

@Slf4j
abstract class SupervisionEventHandler<T extends Supervised> {

  protected SupervisedCacheService<T> service;
  protected C2monCache<T> cache;

  protected SupervisionEventHandler(SupervisedCacheService<T> service) {
    this.service = service;
    this.cache = service.getCache();
  }

  public void onUp(Long id, Timestamp timestamp, String message){
    logMethodEntry("onUp", id, timestamp, message);

    try {
      service.resume(id, timestamp, message);
    } catch (CacheElementNotFoundException cacheEx) {
      log.error("Cannot locate the Supervised object in the cache - unable to update it.", cacheEx);
    }
  }

  /**
   * Called when an DAQ alive timer expires.
   * <p>
   * The onDown() method sets the value of the state tag associated with
   * the supervised object to "DOWN". If the value of the state tag is already "DOWN",
   * no further action is taken.
   * <p>
   * Call within block synchronized on this Supervised obj.
   */
  public void onDown(Long id, Timestamp timestamp, String message) {
    logMethodEntry("onDown", id, timestamp, message);

    service.suspend(id, timestamp, message);

    Long stateTagId = supervised.getStateTagId();


//    TODO (Alex) Review and execute this flow. See also the onUp()

//    try {
//      Long stateTagId = supervised.getStateTagId();
//      if (stateTagId == null) {
//        log.error("State tag Id is set to null for Process + " + processCopy.getId() + " - unable to update it.");
//      } else {
//      if (!filterOut) {
//        tag = dataTagCache.get(...)
//        tag.getDataTagQuality().validate(); // Here we want the opposite for onDown?
//        AbstractTagObjectFacade.updateValue(dataTag, value, valueDesc);
//        setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp);
//      }
//    } catch (CacheElementNotFoundException cacheEx) {
//      log.error("Cannot locate the Process State tag in the cache - unable to update it.", cacheEx);
//    }
  }

  protected void logMethodEntry(String method, Long id, Timestamp timestamp, String message) {
    log.debug(method + "(" + id + ", " + timestamp + ", " + message + ")");
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
