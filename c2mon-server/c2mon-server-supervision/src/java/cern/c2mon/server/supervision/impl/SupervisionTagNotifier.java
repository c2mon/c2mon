package cern.c2mon.server.supervision.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.server.cache.CacheProvider;
import cern.c2mon.server.cache.ClusterCache;
import cern.c2mon.server.cache.DataTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.EquipmentFacade;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.RuleTagCache;
import cern.c2mon.server.cache.TagLocationService;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.supervision.SupervisionAppender;
import cern.c2mon.server.common.component.Lifecycle;
import cern.c2mon.server.common.config.ServerConstants;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.common.rule.RuleTag;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.supervision.SupervisionListener;
import cern.c2mon.server.supervision.SupervisionNotifier;
import cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionStatus;

/**
 * On supervision status changes, calls listeners of all C2monCacheWithSupervision
 * that are registered for Tag update notifications on supervision changes. 
 * 
 * <p>Only passes on DOWN/STOPPED and RUNNING notifications (start-up ignored).
 * 
 * <p>Only a tag lock is held during the notification procedure. The process and
 * equipment locks are accessed when getting copies of the Equipment/Tag lists
 * and when checking the Process/Equipment status'. Tag lock is held while performing
 * the invalidation AND notifying the listeners: this is REQUIRED to prevent successive
 * supervision changes from overtaking each other (the process state is appended while
 * this lock is held). Notice that as a result, there is no guarantee the listener
 * will be notified of all status changes if there are successive change close together
 * (in this case the listener may receive 2 notifications with the latest status only).
 * 
 * <p>Timestamps are not changed when supervision
 * status is added to Tag object: as a result, listeners can filter out supervision
 * callbacks if they are overtaken by a newer incoming value (may happen since
 * many callbacks are made and this could last some time).
 * 
 * <p>Notice that if a cache element is reconfigured during one of these supervision
 * notifications, the corresponding callback may fail for the given element and any
 * dependent elements (eg. Rules dependent on a Tag).
 *  
 * @author Mark Brightwell
 *
 */
@Service
public class SupervisionTagNotifier implements SupervisionListener, SmartLifecycle {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SupervisionTagNotifier.class);   
  
  /**
   * Register for notifications from the SupervisionNotifier bean.
   */
  private SupervisionNotifier supervisionNotifier;
  
  /**
   * For adding supervision info.
   */
  private SupervisionAppender supervisionAppender;
  
  /**
   * Caches used for accessing supervision states.
   */
  private ProcessCache processCache;
  private EquipmentCache equipmentCache;  
  private ProcessFacade processFacade;
  private EquipmentFacade equipmentFacade;
  
  /**
   * Used for locating a Tag in the appropriate Tag cache.
   */
  private TagLocationService tagLocationService;
  
  /**
   * Caches with listeners notified of supervision information (Tag caches).
   */
  private DataTagCache dataTagCache;
  private RuleTagCache ruleTagCache;
  
  /**
   * Caches used to filter out older supervision events, to prevent overtaking of DOWN and
   * UP events *for a given tag*. If a single Tag has already been notified of a more recent
   * event, no more Tags will be notified of older events (avoid using a time for each tag
   * individually!, resulting in large maps).
   * 
   * <p>Lock is used for both maps.
   * 
   * <p>All elements are shared through the cluster
   */
   private final C2monCache<Long, SupervisionEvent> processEventCache;
   private final C2monCache<Long, SupervisionEvent> equipmentEventCache;
  
  /** Cluster cache key lock */
  protected static final String EVENT_LOCK = "c2mon.supervision.SupervisionTagNotifier.eventLock";
  
  /**
   * For lifecycle callback to stop listener threads.
   */
  private Lifecycle listenerContainer;
  
  /**
   * Lifecycle flag.
   */
  private volatile boolean running = false;

  /** For distributed locking on the cluster */
  private final ClusterCache clusterCache;
  
  /**
   * Constructor.
   * @param supervisionNotifier notifier bean
   * @param cacheProvider Reference to the cache provider service
   * @param tagLocationService tag location service
   * @param supervisionAppender 
   *        Reference to helper bean for adding the current supervision status of Processes
   *        and Equipments to Tags
   * @param processFacade process facade bean
   * @param equipmentFacade equipment facade bean
   * @param processEventCache 
   *        Reference to a {@link C2monCache} instance for managing process SuperVisionEvents
   *        also across multiple servers
   * @param equipmentEventCache 
   *        Reference to a {@link C2monCache} instance for managing equipment SuperVisionEvents
   *        also across multiple servers
   */
  @Autowired  
  public SupervisionTagNotifier(final SupervisionNotifier supervisionNotifier,
                                final CacheProvider cacheProvider,
                                final TagLocationService tagLocationService,
                                final SupervisionAppender supervisionAppender,
                                final ProcessFacade processFacade,
                                final EquipmentFacade equipmentFacade,
                                @Qualifier("processEventCache") final C2monCache<Long, SupervisionEvent> processEventCache,
                                @Qualifier("equipmentEventCache") final C2monCache<Long, SupervisionEvent> equipmentEventCache) {
    super();
    this.supervisionNotifier = supervisionNotifier;
    this.tagLocationService = tagLocationService;
    this.supervisionAppender = supervisionAppender;
    this.processFacade = processFacade;
    this.equipmentFacade = equipmentFacade;
    this.processEventCache = processEventCache;
    this.equipmentEventCache = equipmentEventCache;
    
    this.processCache = cacheProvider.getProcessCache();
    this.equipmentCache = cacheProvider.getEquipmentCache();
    this.dataTagCache = cacheProvider.getDataTagCache();
    this.ruleTagCache = cacheProvider.getRuleTagCache();
    this.clusterCache = cacheProvider.getClusterCache();
  }

  /**
   * Run on bean creation. Registers with SupervisionNotifier.
   */
  @PostConstruct
  public void init() {
    listenerContainer = supervisionNotifier.registerAsListener(this, 10);
    //set initial supervision states from cache
    //lock in case other server is running
    clusterCache.acquireWriteLockOnKey(EVENT_LOCK);
    try {
      for (Long key : processCache.getKeys()) {
        if (!processEventCache.hasKey(key)) {
          processEventCache.put(key, processFacade.getSupervisionStatus(key));
        }
      }
      for (Long key : equipmentCache.getKeys()) {
        if (!equipmentEventCache.hasKey(key)) {          
          equipmentEventCache.put(key, equipmentFacade.getSupervisionStatus(key));
        }
      }
    } finally {
      clusterCache.releaseWriteLockOnKey(EVENT_LOCK);
    }    
  }
  
  @Override
  public void notifySupervisionEvent(final SupervisionEvent event) {  
    SupervisionStatus status = event.getStatus();
    Long entityId = event.getEntityId();
    if (status.equals(SupervisionStatus.RUNNING) || status.equals(SupervisionStatus.DOWN) || status.equals(SupervisionStatus.STOPPED)) {
      
      //lock for if-else logic only
      clusterCache.acquireWriteLockOnKey(EVENT_LOCK);
      try {
        switch (event.getEntity()) {
        case PROCESS:
          if (!processEventCache.hasKey(entityId) || !processEventCache.get(entityId).getEventTime().after(event.getEventTime()))           
            processEventCache.put(entityId, event);
          break;
        case EQUIPMENT:
          if (!equipmentEventCache.hasKey(entityId) || !equipmentEventCache.get(entityId).getEventTime().after(event.getEventTime()))                        
            equipmentEventCache.put(event.getEntityId(), event);
          break;
        default:          
          break; //do nothing for sub-equipment
        }
      } finally {
        clusterCache.releaseWriteLockOnKey(EVENT_LOCK);
      }
      
      switch (event.getEntity()) {
      case PROCESS :        
        notifyProcessTags(entityId);
        break;      
      case EQUIPMENT:       
        notifyEquipmentTags(entityId);
        break;        
      default: 
        break; //do nothing for subequipment
      }
    }    
  }

  /**
   * Notifies all equipments under this process. Will use event in local map. 
   * @param processId process id
   */
  private void notifyProcessTags(final Long processId) {
    Process process = processCache.getCopy(processId);
    for (Long equipmentId : process.getEquipmentIds()) { //no lock required as get copy
      notifyEquipmentTags(equipmentId);
    }    
  }

  /**
   * Calls notification method for all tags associated to the Equipment (DataTags only).
   * @param equipementId the equipment id
   */
  private void notifyEquipmentTags(final Long equipementId) {
    try {      
      //local map so as not to notify rules twice; lock on map when modifying
      Map<Long, Boolean> notifiedRules = new HashMap<Long, Boolean>();
      Equipment equipment = equipmentCache.getCopy(equipementId); //may have been removed in the meantime!      
      Collection<Long> tagIds = equipment.getDataTagIds(); 
      for (Long id : tagIds) {
       try {        
         callCacheNotification(id, notifiedRules); //recursively notifies all dependent rules also, once only                  
       } catch (CacheElementNotFoundException cacheEx) {
         LOGGER.warn("Unable to locate Tag/Rule cache element during Tag supervision " //TODO ask DAQ refresh
             + "change callback (some Tags/Rules may have been omitted)", cacheEx);
       }              
      }                   
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.warn("Unable to locate Equipment element during Tag supervision "
          + "change callback (so no invalidation callbacks performed for associated Tags)", cacheEx);
    }
  }
  
  /**
   * Private recursive method for calling all listeners; recursive calls for
   * calling the notification for all dependent rules also.
   * @param id tag id
   * @param notifiedRules map for preventing multiple notifications for rules
   */
  private void callCacheNotification(final Long id, final Map<Long, Boolean> notifiedRules) {        
    synchronized (notifiedRules) {
      Tag tagCopy = tagLocationService.getCopy(id);
      if (!notifiedRules.containsKey(tagCopy.getId())) {
        LOGGER.trace("Performing supervision notification for tag " + id);
        boolean dirtyTagContext = false;
                 
        for (Long procId : tagCopy.getProcessIds()) {
          if (processEventCache.hasKey(procId)) { //null never override a value, so if statement ok out of lock
            supervisionAppender.addSupervisionQuality(tagCopy, processEventCache.getCopy(procId));
            dirtyTagContext = true;
          }              
        }
        for (Long eqId : tagCopy.getEquipmentIds()) {
          if (equipmentEventCache.hasKey(eqId)) {
            supervisionAppender.addSupervisionQuality(tagCopy, equipmentEventCache.getCopy(eqId));
            dirtyTagContext = true;
          }
        }
        
        if (dirtyTagContext) {                     
          if (tagCopy instanceof DataTag) {
            dataTagCache.notifyListenersOfSupervisionChange((DataTag) tagCopy);
          } else if (tagCopy instanceof RuleTag) {
            ruleTagCache.notifyListenersOfSupervisionChange((RuleTag) tagCopy);
          } else {
            throw new IllegalArgumentException("Unexpected call with Tag parameter that is neither DataTag or RuleTag; "
                + "type is " + tagCopy.getClass().getSimpleName());
          }
        }
      }
      
      Collection<Long> ruleIds;
      ruleIds = new ArrayList<Long>(tagCopy.getRuleIds());                 
      for (Long ruleId : ruleIds) {      
        callCacheNotification(ruleId, notifiedRules);
        notifiedRules.put(ruleId, true);     
      }      
    }          
  }
  
  @Override
  public boolean isAutoStartup() {   
    return false;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public void start() {
    LOGGER.debug("Starting SupervisionTagNotifier");
    running = true;
    listenerContainer.start();
  }

  @Override
  public void stop() {
    LOGGER.debug("Stopping SupervisionTagNotifier");
    listenerContainer.stop();
    running = false;    
  }

  @Override
  public int getPhase() {
    return ServerConstants.PHASE_STOP_LAST + 1;    
  }
  
}
