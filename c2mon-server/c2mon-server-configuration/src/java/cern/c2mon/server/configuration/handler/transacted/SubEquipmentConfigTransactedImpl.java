package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.cache.AliveTimerCache;
import cern.tim.server.cache.CommFaultTagCache;
import cern.tim.server.cache.EquipmentCache;
import cern.tim.server.cache.ProcessCache;
import cern.tim.server.cache.SubEquipmentCache;
import cern.tim.server.cache.SubEquipmentFacade;
import cern.tim.server.cache.loading.SubEquipmentDAO;
import cern.tim.server.common.subequipment.SubEquipment;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;
import cern.tim.shared.common.ConfigurationException;

/**
 * See interface docs.
 * 
 * @author Mark Brightwell
 * 
 */
@Service
public class SubEquipmentConfigTransactedImpl extends AbstractEquipmentConfigTransacted<SubEquipment> implements SubEquipmentConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(SubEquipmentConfigTransactedImpl.class);

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

  private EquipmentCache equipmentCache;

  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler, SubEquipmentFacade subEquipmentFacade,
      SubEquipmentCache subEquipmentCache, SubEquipmentDAO subEquipmentDAO, AliveTimerCache aliveTimerCache, CommFaultTagCache commFaultTagCache,
      ProcessCache processCache, EquipmentCache equipmentCache) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache, subEquipmentDAO, aliveTimerCache, commFaultTagCache);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentCache = subEquipmentCache;
    this.subEquipmentDAO = subEquipmentDAO;
    this.equipmentCache = equipmentCache;
  }

  /**
   * Creates the SubEquipment cache object and puts it into the cache and DB.
   * The alive and commfault tag caches are updated also. The Equipment cache
   * object is updated to include the new SubEquipment.
   * 
   * @param element
   *          details of configuration
   * @throws IllegalAccessException
   *           should not be thrown here (in common interface for Tags)
   */
  @Override
  @Transactional("cacheTransactionManager")
  public ProcessChange doCreateSubEquipment(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
    return new ProcessChange(subEquipmentFacade.getEquipmentForSubEquipment(subEquipment.getId()).getProcessId());
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> doUpdateAbstractEquipment(final SubEquipment subEquipment, Properties properties) throws IllegalAccessException {
    return super.updateAbstractEquipment(subEquipment, properties);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveSubEquipment(final SubEquipment subEquipment, final ConfigurationElementReport subEquipmentReport) {
    try {
      subEquipmentDAO.deleteItem(subEquipment.getId());
      return new ProcessChange(equipmentCache.get(subEquipment.getParentId()).getProcessId());
    } catch (RuntimeException e) {
      subEquipmentReport.setFailure("Rolling back removal of sub-equipment " + subEquipment.getId());
      throw new UnexpectedRollbackException("Exception caught while removing Sub-equipment from DB: rolling back", e);
    }
  }

}
