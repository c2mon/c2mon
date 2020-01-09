package cern.c2mon.server.supervision.impl.event;

import cern.c2mon.cache.actions.supervision.SupervisedCacheService;
import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.supervision.Supervised;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Slf4j
abstract class SupervisionEventHandler<T extends Supervised> {

  // You can technically modify this, but don't - the self registration in Ctor is all that's needed
  @Getter
  private static Map<Class<? extends Supervised>, SupervisionEventHandler<? extends Supervised>> eventHandlers = new HashMap<>();
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
   */
  public void onDown(Long id, Timestamp timestamp, String message) {
    service.suspend(id, timestamp, message);

//  TODO (Alex) Review this flow? See also the onUp()

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
}
