package cern.c2mon.server.configuration.handler.impl;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.CommonEquipmentConfigTransacted;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.C2monCache;
import cern.c2mon.server.cache.equipment.CommonEquipmentFacade;
import cern.c2mon.server.common.equipment.AbstractEquipment;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.common.ConfigurationException;

/**
 * Common part of Equipment-SubEquipment handler.
 * 
 * @author Mark Brightwell
 *
 * @param <T> type of Equipment
 */
public abstract class AbstractEquipmentConfigHandler<T extends AbstractEquipment> {

  private static final Logger LOGGER = Logger.getLogger(AbstractEquipmentConfigHandler.class); 
  
  private ControlTagConfigHandler controlTagConfigHandler;
  
  private CommonEquipmentConfigTransacted<T> abstractEquipmentConfigTransacted;
  
  private C2monCache<Long, T> abstractEquipmentCache;
  
  private AliveTimerCache aliveTimerCache;

  private CommFaultTagCache commFaultTagCache;
  
  private CommonEquipmentFacade<T> commonEquipmentFacade;
  
  /**
   * Constructor called from implementation and setting required beans.
   */
  public AbstractEquipmentConfigHandler(ControlTagConfigHandler controlTagConfigHandler,
                                          CommonEquipmentConfigTransacted<T> abstractEquipmentConfigTransacted,
                                          C2monCache<Long, T> abstractEquipmentCache,
                                          AliveTimerCache aliveTimerCache,
                                          CommFaultTagCache commFaultTagCache,
                                          CommonEquipmentFacade<T> commonEquipmentFacade) {
    super();
    this.controlTagConfigHandler = controlTagConfigHandler;
    this.abstractEquipmentConfigTransacted = abstractEquipmentConfigTransacted;
    this.abstractEquipmentCache = abstractEquipmentCache;
    this.aliveTimerCache = aliveTimerCache;
    this.commFaultTagCache = commFaultTagCache;
    this.commonEquipmentFacade = commonEquipmentFacade;
  }

  /**
   * Removes the control tags for this equipment. Notice that if this fails, the
   * equipment object will still be removed: this is to prevent the situation of
   * not being able to remove the equipment because of the control tags (say if another
   * equipment is also using them by mistake) and not being able to remove the
   * control tags because of the equipment.
   * 
   * <p>Notice that in case of failure, only part of the control tags could remain; they
   * are removed in the following order: Alive tag, CommFaultTag, State tag.
   *  
   * @param abstractEquipment the AbstracEquipment to remove
   * @param equipmentReport for adding the subreports to
   */
  protected void removeEquipmentControlTags(final T abstractEquipment, final ConfigurationElementReport equipmentReport) {
    LOGGER.debug("Removing (Sub-)Equipment control tags.");
    Long aliveTagId = abstractEquipment.getAliveTagId();
    if (aliveTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, aliveTagId);
      equipmentReport.addSubReport(tagReport);
      controlTagConfigHandler.removeControlTag(aliveTagId, tagReport);      
    }    
    Long commTagId = abstractEquipment.getCommFaultTagId();
    if (commTagId != null) {
      ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, commTagId);
      equipmentReport.addSubReport(tagReport);
      controlTagConfigHandler.removeControlTag(commTagId, tagReport);     
    }    
    Long stateTagId = abstractEquipment.getStateTagId();
    ConfigurationElementReport tagReport = new ConfigurationElementReport(Action.REMOVE, Entity.CONTROLTAG, stateTagId);
    equipmentReport.addSubReport(tagReport);
    controlTagConfigHandler.removeControlTag(stateTagId, tagReport);    
  }
  
  /**
   * Common part of (Sub-)Equipment update method. Mainly deals
   * with rollback of other cache changes in case of failure.
   * 
   * @param abstractEquipmentId id of (sub)equipment
   * @param elementProperties properties with update details
   * @return changes to be sent to the DAQ layer
   * @throws IllegalAccessException if thrown when updating fields
   */
  protected List<ProcessChange> commonUpdate(Long abstractEquipmentId, Properties elementProperties) throws IllegalAccessException {
    LOGGER.debug("Updating (Sub)Equipment " + abstractEquipmentId);
    // TODO or not todo: warning: can still update commfault, alive and state
    // tag id to non-existent tags (id is NOT checked and exceptions will be
    // thrown!)

    // do not allow id changes! (they would not be applied in any case)
    if (elementProperties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, "Attempting to change the (sub)equipment id - this is not currently supported!");
    }
    boolean aliveConfigure = false;
    if (elementProperties.containsKey("aliveInterval") || elementProperties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }
    boolean commFaultConfigure = false;
    if (elementProperties.containsKey("commFaultTagId")) {
      commFaultConfigure = true;
    }        
    abstractEquipmentCache.acquireWriteLockOnKey(abstractEquipmentId);
    try {
      T abstractEquipment = abstractEquipmentCache.get(abstractEquipmentId);        
      try {
        Long oldAliveId = abstractEquipment.getAliveTagId();
        Long oldCommFaultId = abstractEquipment.getCommFaultTagId();
        List<ProcessChange> processChanges = abstractEquipmentConfigTransacted.doUpdateAbstractEquipment(abstractEquipment, elementProperties);
        abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);
        if (aliveConfigure) {
          if (oldAliveId != null)
            commonEquipmentFacade.removeAliveDirectly(oldAliveId);
          if (abstractEquipment.getAliveTagId() != null)
            commonEquipmentFacade.loadAndStartAliveTag(abstractEquipment.getId());
        }
        if (commFaultConfigure && abstractEquipment.getCommFaultTagId() != null) {
          if (oldCommFaultId != null)
            commFaultTagCache.remove(oldCommFaultId);
          if (abstractEquipment.getCommFaultTagId() != null)
            commFaultTagCache.loadFromDb(abstractEquipment.getCommFaultTagId());
        }
        return processChanges;
      } catch (RuntimeException ex) {
        LOGGER.error("Exception caught while updating Sub-equipment - rolling back changes to the Sub-equipment", ex);
        //reload all potentially updated cache elements now DB changes are rolled back 
        if (abstractEquipmentCache.isWriteLockedByCurrentThread(abstractEquipmentId))
          abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);
        commFaultTagCache.remove(abstractEquipment.getCommFaultTagId());      
        aliveTimerCache.remove(abstractEquipment.getAliveTagId());
        abstractEquipmentCache.remove(abstractEquipmentId);
        T oldAbstractEquipment = abstractEquipmentCache.get(abstractEquipmentId);
        commFaultTagCache.loadFromDb(oldAbstractEquipment.getCommFaultTagId());      
        commonEquipmentFacade.loadAndStartAliveTag(abstractEquipmentId); //reloads alive from DB
        throw ex;
      }
    } finally {
      if (abstractEquipmentCache.isWriteLockedByCurrentThread(abstractEquipmentId)) 
        abstractEquipmentCache.releaseWriteLockOnKey(abstractEquipmentId);
    }
  }
  
}
