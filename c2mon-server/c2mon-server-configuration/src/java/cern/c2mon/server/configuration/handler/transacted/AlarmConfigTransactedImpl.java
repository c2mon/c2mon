package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.impl.TagConfigGateway;
import cern.c2mon.server.cache.AlarmCache;
import cern.c2mon.server.cache.AlarmFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.cache.loading.AlarmLoaderDAO;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Implementation of transacted methods.
 * 
 * @author Mark Brightwell
 *
 */
@Service
public class AlarmConfigTransactedImpl implements AlarmConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(AlarmConfigTransactedImpl.class);

  /**
   * Reference to the alarm facade.
   */
  private AlarmFacade alarmFacade;
  
  /**
   * Reference to the alarm DAO.
   */
  private AlarmLoaderDAO alarmDAO;
  
  /**
   * Reference to the alarm cache.
   */
  private AlarmCache alarmCache;
  
  /**
   * Reference to gateway to tag configuration beans.
   */
  @Autowired
  private TagConfigGateway tagConfigGateway;
  
  /**
   * Autowired constructor.
   * @param alarmFacade the alarm facade bean
   * @param alarmDAO the alarm DAO bean
   * @param alarmCache the alarm cache bean
   * @param tagConfigGateway the tag configuration gateway bean
   */
  @Autowired
  public AlarmConfigTransactedImpl(final AlarmFacade alarmFacade, final AlarmLoaderDAO alarmDAO, 
                            final AlarmCache alarmCache) {
    super();
    this.alarmFacade = alarmFacade;
    this.alarmDAO = alarmDAO;
    this.alarmCache = alarmCache;
  }

  /**
   * Creates an alarm object in the server (puts in DB and loads into cache,
   * in that order, and updates the associated tag to point to the new
   * alarm).
   * 
   * @param element the details of the new alarm object
   * @throws IllegalAccessException should not throw the {@link IllegalAccessException} (only Tags can).
   */
  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRED)
  public void doCreateAlarm(final ConfigurationElement element) throws IllegalAccessException {
    LOGGER.trace("Creating alarm " + element.getEntityId());
    Alarm alarm = alarmFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
    try {
      alarmDAO.insert(alarm);
    } catch (Exception e) {
      LOGGER.error("Exception caught while inserting a new Alarm into the DB - rolling back changes", e);
      throw new UnexpectedRollbackException("Unexpected exception while creating an Alarm: rolling back the change", e);
    }    
    try {
      alarmCache.putQuiet(alarm);
      //add alarm to tag in cache (no DB persistence here)
      tagConfigGateway.addAlarmToTag(alarm.getTagId(), alarm.getId());
    } catch (Exception e) {
      LOGGER.error("Exception caught while loading a new Alarm", e);
      alarmCache.remove(alarm.getId());
      tagConfigGateway.removeAlarmFromTag(alarm.getTagId(), alarm.getId());
      throw new UnexpectedRollbackException("Unexpected exception while creating an Alarm: rolling back the creation", e);
    }
    
  }

  /**
   * Updates the Alarm object in the server from the provided Properties.
   * In more detail, updates the cache, then the DB.
   * 
   * <p>Note that moving the alarm to a different tag is not allowed. In
   * this case the alarm should be removed and recreated.
   * @param alarmId the id of the alarm
   * @param properties the update details
   */
  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public void doUpdateAlarm(final Long alarmId, final Properties properties) {
    //reject if trying to change datatag it is attached to - not currently allowed
    if (properties.containsKey("dataTagId")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, 
          "Attempting to change the tag to which the alarm is attached - this is not currently supported!");
    }        
    alarmCache.acquireWriteLockOnKey(alarmId);    
    try {
      Alarm alarm = alarmCache.get(alarmId);
      alarmFacade.updateConfig(alarm, properties);
      alarmDAO.updateConfig(alarm);
    } catch (CacheElementNotFoundException ex) {
      throw ex;
    } catch (Exception ex) {      
      LOGGER.error("Exception caught while updating alarm" + alarmId, ex);
      throw new UnexpectedRollbackException("Unexpected exception caught while updating Alarm " + alarmId, ex);
    } finally {
      alarmCache.releaseWriteLockOnKey(alarmId);
    }            
  }
 
  @Override
  @Transactional(value = "cacheTransactionManager", propagation=Propagation.REQUIRES_NEW)
  public void doRemoveAlarm(final Long alarmId, final ConfigurationElementReport alarmReport) {     
    alarmCache.acquireWriteLockOnKey(alarmId);
    try {
      Alarm alarm = alarmCache.get(alarmId);     
      alarmDAO.deleteItem(alarmId);
      alarmCache.releaseWriteLockOnKey(alarmId); //unlock before locking tag
      try {
        removeDataTagReference(alarm);
      } catch (CacheElementNotFoundException e) {
        LOGGER.warn("Unable to remove Alarm reference from Tag, as could not locate Tag " + alarm.getTagId() + " in cache");
      }
    } catch (CacheElementNotFoundException e) {
      LOGGER.debug("Attempting to remove a non-existent Alarm - no action taken.");
      alarmReport.setWarning("Attempting to remove a non-existent Alarm");      
    } catch (Exception ex) {      
      LOGGER.error("Exception caught while removing Alarm " + alarmId, ex);
      alarmReport.setFailure("Unable to remove Alarm with id " + alarmId);
      throw new UnexpectedRollbackException("Exception caught while attempting to remove an alarm", ex);
    } finally {
      if (alarmCache.isWriteLockedByCurrentThread(alarmId)) {
        alarmCache.releaseWriteLockOnKey(alarmId);
      }        
    }      
  }

  /**
   * Removes the reference to the alarm in the associated Tag object.
   * @param alarm the alarm for which the tag needs updating
   */
  private void removeDataTagReference(final Alarm alarm) { 
    tagConfigGateway.removeAlarmFromTag(alarm.getTagId(), alarm.getId());
  }
  
}
