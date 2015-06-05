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
import cern.c2mon.server.cache.ControlTagCache;
import cern.c2mon.server.cache.ControlTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.cache.SubEquipmentFacade;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.common.control.ControlTag;
import cern.c2mon.server.common.control.ControlTagCacheObject;
import cern.c2mon.server.common.equipment.Equipment;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.configuration.handler.ControlTagConfigHandler;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.daq.config.DataTagAdd;
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
  
  private ControlTagCache controlCache;
  
  private ControlTagFacade controlTagFacade;

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
                                          ProcessXMLProvider processXMLProvider,
                                          ControlTagCache controlCache, 
                                          ControlTagFacade controlTagFacade) {
    super(controlTagConfigHandler, subEquipmentFacade, subEquipmentCache, subEquipmentDAO, aliveTimerCache, commFaultTagCache);
    this.subEquipmentFacade = subEquipmentFacade;
    this.subEquipmentDAO = subEquipmentDAO;
    this.equipmentCache = equipmentCache;
    this.processXMLProvider = processXMLProvider;
    this.controlCache = controlCache;
    this.controlTagFacade = controlTagFacade;
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
  public List<ProcessChange> doCreateSubEquipment(final ConfigurationElement element) throws IllegalAccessException {
    SubEquipment subEquipment = super.createAbstractEquipment(element);
    subEquipmentFacade.addSubEquipmentToEquipment(subEquipment.getId(), subEquipment.getParentId());
    SubEquipmentUnitAdd subEquipmentUnitAdd = new SubEquipmentUnitAdd(element.getSequenceId(), subEquipment.getId(), subEquipment.getParentId(),
        processXMLProvider.getSubEquipmentConfigXML((SubEquipmentCacheObject) subEquipment));
    List<ProcessChange> changes = new ArrayList<ProcessChange>();
    changes.add(new ProcessChange(equipmentCache.get(subEquipment.getParentId()).getProcessId(), subEquipmentUnitAdd));
    changes.addAll(ensureCtrlTagsSet(element, subEquipment));
    return changes;
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> doUpdateAbstractEquipment(final SubEquipment subEquipment, Properties properties) throws IllegalAccessException {
    return super.updateAbstractEquipment(subEquipment, properties);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public List<ProcessChange> doRemoveSubEquipment(final SubEquipment subEquipment, final ConfigurationElementReport subEquipmentReport) {
    List<ProcessChange> processChanges = new ArrayList<ProcessChange>();

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
  
  
  /**
   * Ensures that the Alive, State and CommFault Tags are set appropriately in the {@link ControlTagCache}.
   * @param subEquipment 
   * @throws IllegalAccessException
   */
  private List<ProcessChange> ensureCtrlTagsSet(final ConfigurationElement element, final SubEquipment subEquipment) throws IllegalAccessException {
      
      List<ProcessChange> changes = new ArrayList<ProcessChange>(3);
      Equipment equipment = this.equipmentCache.get(subEquipment.getParentId());
      
      ControlTag aliveTagCopy = controlCache.getCopy(subEquipment.getAliveTagId());
      if (aliveTagCopy != null) {
        LOGGER.trace("Setting correct aliveTag in SubEquipment " + subEquipment.getName());
        ((ControlTagCacheObject)aliveTagCopy).setSubEquipmentId(subEquipment.getId());
        ((ControlTagCacheObject)aliveTagCopy).setProcessId(equipment.getProcessId());
        controlCache.putQuiet(aliveTagCopy);
        DataTagAdd toAdd = new DataTagAdd(element.getSequenceId(), subEquipment.getId(), controlTagFacade.generateSourceDataTag(aliveTagCopy));
        changes.add(new ProcessChange(equipment.getProcessId(), toAdd));
      } else {
        // TODO change to ConfigurationException
        throw new IllegalArgumentException("No alive tag (" + subEquipment.getAliveTagId() + ") found for subequipment " + subEquipment.getName());
      }
      
      ControlTag commFaultTagCopy = controlCache.getCopy(subEquipment.getCommFaultTagId());
      if (commFaultTagCopy != null) {
        LOGGER.trace("Setting correct commFaultTag in SubEquipment " + subEquipment.getName());
        ((ControlTagCacheObject)commFaultTagCopy).setSubEquipmentId(subEquipment.getId());
        ((ControlTagCacheObject)commFaultTagCopy).setProcessId(equipment.getProcessId());
        controlCache.putQuiet(commFaultTagCopy);
      } else {
        // TODO change to ConfigurationException
        throw new IllegalArgumentException("No commfault tag (" + subEquipment.getCommFaultTagId() + ") found for subequipment " + subEquipment.getName());
      }
      
      ControlTag statusTagCopy = controlCache.getCopy(subEquipment.getStateTagId());
      if (statusTagCopy != null) {
        LOGGER.trace("Setting correct statusTagCopy in SubEquipment " + subEquipment.getName());
        ((ControlTagCacheObject)statusTagCopy).setSubEquipmentId(subEquipment.getId());
        ((ControlTagCacheObject)statusTagCopy).setProcessId(equipment.getProcessId());
        controlCache.putQuiet(statusTagCopy);
      } else {
        // TODO change to ConfigurationException
        throw new IllegalArgumentException("No status tag (" + subEquipment.getStateTagId() + ") found for subequipment " + subEquipment.getName());
      }
      
      
      return changes;
  }
  

}
