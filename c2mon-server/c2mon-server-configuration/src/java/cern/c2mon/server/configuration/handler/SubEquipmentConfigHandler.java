package cern.c2mon.server.configuration.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.AliveTimerFacade;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.CommFaultTagFacade;
import cern.tim.server.cache.SubEquipmentCache;
import cern.tim.server.cache.SubEquipmentFacade;
import cern.tim.server.cache.loading.SubEquipmentDAO;
import cern.tim.server.common.subequipment.SubEquipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * Class managing the SubEquipment configuration.
 * @author Mark Brightwell
 *
 */
@Service
public class SubEquipmentConfigHandler extends AbstractEquipmentConfigHandler<SubEquipment> {

  /**
   * Facade.
   */
  private SubEquipmentFacade subEquipmentFacade;
  
  /**
   * Cache.
   */
  private SubEquipmentCache subEquipmentCache;
  
  /**
   * DAO.
   */
  private SubEquipmentDAO subEquipmentDAO;
  
  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigHandler(ControlTagConfigHandler controlTagConfigHandler, SubEquipmentFacade subEquipmentFacade,
                                   SubEquipmentCache subEquipmentCache, SubEquipmentDAO subEquipmentDAO,
                                   AliveTimerFacade aliveTimerFacade, CommFaultTagFacade commFaultTagFacade) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache,
          subEquipmentDAO, aliveTimerFacade, commFaultTagFacade);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
  }

  /**
   * Creates the SubEquipment cache object and puts it into the cache
   * and DB. The alive and commfault tag caches are updated also.
   * The Equipment cache object is updated to include the new
   * SubEquipment.
   * 
   * @param element details of configuration
   * @throws IllegalAccessException should not be thrown here (in common interface for Tags)
   */
  public void createSubEquipment(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
  }

  /**
   * First removes the SubEquipment from the DB and cache. If successful,
   * removes the associated control tags. 
   * @param subEquipmentId id
   * @param subEquipmentReport to which subreports may be added
   */
  public void removeSubEquipment(final Long subEquipmentId, final ConfigurationElementReport subEquipmentReport) {
    SubEquipment subEquipment = subEquipmentCache.get(subEquipmentId);    
    try {      
      subEquipment.getWriteLock().lock();      
      subEquipmentDAO.deleteItem(subEquipmentId);
      subEquipmentFacade.removeCacheObject(subEquipmentCache.get(subEquipmentId));
      removeEquipmentControlTags(subEquipment, subEquipmentReport); //must be after removal of subequipment from DB
    } finally {
      subEquipment.getWriteLock().unlock();
    }    
  }

}
