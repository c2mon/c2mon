package cern.c2mon.server.configuration.handler.transacted;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.AliveTimerCache;
import cern.c2mon.server.cache.CommFaultTagCache;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.SubEquipmentUnitAdd;
import cern.c2mon.shared.daq.config.SubEquipmentUnitRemove;

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
   * DAO.
   */
  private SubEquipmentDAO subEquipmentDAO;

  private EquipmentCache equipmentCache;

  private ProcessXMLProvider processXMLProvider;

  /**
   * Autowired constructor.
   */
  @Autowired
  public SubEquipmentConfigTransactedImpl(ControlTagConfigHandler controlTagConfigHandler,
                                          SubEquipmentFacade subEquipmentFacade,
                                          SubEquipmentCache subEquipmentCache,
                                          SubEquipmentDAO subEquipmentDAO,
                                          AliveTimerCache aliveTimerCache,
                                          CommFaultTagCache commFaultTagCache,
                                          ProcessCache processCache,
                                          EquipmentCache equipmentCache,
                                          ProcessXMLProvider processXMLProvider) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache, subEquipmentDAO, aliveTimerCache, commFaultTagCache);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentDAO = subEquipmentDAO;
    this.equipmentCache = equipmentCache;
    this.processXMLProvider = processXMLProvider;
  }

  /**
   * Creates the SubEquipment cache object and puts it into the cache and DB.
   * The alive and commfault tag caches are updated also. The Equipment cache
   * object is updated to include the new SubEquipment.
   *
   * @param element details of configuration
   * @throws IllegalAccessException should not be thrown here (in common
   *           interface for Tags)
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateSubEquipment(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
    SubEquipmentUnitAdd subEquipmentUnitAdd = new SubEquipmentUnitAdd(element.getSequenceId(), subEquipment.getId(), subEquipment.getParentId(),
        processXMLProvider.getSubEquipmentConfigXML((SubEquipmentCacheObject) subEquipment));
    return new ProcessChange(equipmentCache.get(subEquipment.getParentId()).getProcessId(), subEquipmentUnitAdd);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> doUpdateAbstractEquipment(final SubEquipment subEquipment, Properties properties) throws IllegalAccessException {
    return super.updateAbstractEquipment(subEquipment, properties);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> doRemoveSubEquipment(final SubEquipment subEquipment, final ConfigurationElementReport subEquipmentReport) {
    List<ProcessChange> processChanges = new ArrayList<>();

    try {
      subEquipmentDAO.deleteItem(subEquipment.getId());
      Long processId = equipmentCache.get(subEquipment.getParentId()).getProcessId();

      SubEquipmentUnitRemove subEquipmentUnitRemove = new SubEquipmentUnitRemove(0L, subEquipment.getId(), subEquipment.getParentId());
      processChanges.add(new ProcessChange(processId, subEquipmentUnitRemove));

    } catch (RuntimeException e) {
      subEquipmentReport.setFailure("Rolling back removal of sub-equipment " + subEquipment.getId());
      throw new UnexpectedRollbackException("Exception caught while removing Sub-equipment from DB: rolling back", e);
    }

    return processChanges;
  }

}
