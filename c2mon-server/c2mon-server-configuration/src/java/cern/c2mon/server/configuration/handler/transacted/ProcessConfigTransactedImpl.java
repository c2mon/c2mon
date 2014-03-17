package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessFacade;
import cern.c2mon.server.cache.loading.ProcessDAO;
import cern.c2mon.server.common.process.Process;
import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.server.daqcommunication.in.JmsContainerManager;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.daq.config.Change;

/**
 * See interface docs.
 * 
 * @author Mark Brightwell
 * 
 */
@Service
public class ProcessConfigTransactedImpl implements ProcessConfigTransacted {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(ProcessConfigTransactedImpl.class);

  /**
   * Reference to facade.
   */
  private ProcessFacade processFacade;

  /**
   * Reference to cache.
   */
  private ProcessCache processCache;

  /**
   * Reference to DAO.
   */
  private ProcessDAO processDAO;

  /**
   * Autowired constructor.
   * 
   * @param processFacade the facade bean
   * @param processCache the cache bean
   * @param processDAO the DAO bean
   * @param equipmentConfigHandler the Equipment configuration bean
   * @param controlTagConfigHandler the ControlTag configuration bean
   * @param jmsContainerManager JmsContainerManager bean
   */
  @Autowired
  public ProcessConfigTransactedImpl(final ProcessFacade processFacade, final ProcessCache processCache, final ProcessDAO processDAO,
      final JmsContainerManager jmsContainerManager) {
    super();
    this.processFacade = processFacade;
    this.processCache = processCache;
    this.processDAO = processDAO;
  }

  /**
   * Creates the process and inserts it into the cache and DB (DB first).
   * 
   * <p>
   * Changing a process id is not currently allowed.
   * 
   * @param element
   *          the configuration element
   * @throws IllegalAccessException
   *           not thrown (inherited from common facade interface)
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doCreateProcess(final ConfigurationElement element) throws IllegalAccessException {
    processCache.acquireWriteLockOnKey(element.getEntityId());
    try {
      Process process = (Process) processFacade.createCacheObject(element.getEntityId(), element.getElementProperties());
      processDAO.insert(process);
      processCache.putQuiet(process);
      return new ProcessChange(process.getId());
    } finally {
      processCache.releaseWriteLockOnKey(element.getEntityId());
    }
  }

  /**
   * No changes to the Process configuration are currently passed to the DAQ
   * layer, but the Configuration object is already build into the logic below
   * (always empty and hence ignored in the {@link ConfigurationLoader}).
   * 
   * @param id
   * @param properties
   * @return change requiring DAQ reboot, but not to be sent to the DAQ layer
   *         (not supported)
   * @throws IllegalAccessException
   */
  @Override
  @Transactional(value = "cacheTransactionManager")
  public ProcessChange doUpdateProcess(final Long id, final Properties properties) throws IllegalAccessException {
    if (properties.containsKey("id")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, "Attempting to change the process id - this is not currently supported!");
    }
    if (properties.containsKey("name")) {
      throw new ConfigurationException(ConfigurationException.UNDEFINED, "Attempting to change the process name - this is not currently supported!");
    }
    boolean aliveConfigure = false;
    if (properties.containsKey("aliveInterval") || properties.containsKey("aliveTagId")) {
      aliveConfigure = true;
    }
    Change processUpdate;    
    processCache.acquireWriteLockOnKey(id);
    try {
      Process process = processCache.get(id);
      try {                
        if (aliveConfigure) {
          processFacade.removeAliveTimer(process.getId());
        }
        processUpdate = processFacade.updateConfig(process, properties); // always
                                                                         // empty
                                                                         // return
        processDAO.updateConfig(process);
        processCache.releaseWriteLockOnKey(id);
        if (aliveConfigure) {
          processFacade.loadAndStartAliveTag(process.getId());
        }
      } catch (RuntimeException e) {
        LOGGER.error("Exception caught while updating a new Process - rolling back DB changes and removing from cache.");
        processCache.remove(id);
        if (aliveConfigure) {
          processFacade.removeAliveTimer(process.getId());
        }
        throw new UnexpectedRollbackException("Unexpected exception caught while updating a Process configuration.", e);
      }   
    } finally {
      if (processCache.isWriteLockedByCurrentThread(id)) {
        processCache.releaseWriteLockOnKey(id);
      }
    }
    return new ProcessChange(id);
  }

  @Override
  @Transactional(value = "cacheTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public ProcessChange doRemoveProcess(final Process process, final ConfigurationElementReport processReport) {
    processDAO.deleteProcess(process.getId());
    return new ProcessChange();
  }

  @Override
  public void removeEquipmentFromProcess(Long equipmentId, Long processId) {
    LOGGER.debug("Removing Process Equipments for process " + processId);
    try {      
      processCache.acquireWriteLockOnKey(processId);
      try {
        Process process = processCache.get(processId);
        process.getEquipmentIds().remove(equipmentId);
      } finally {
        processCache.releaseWriteLockOnKey(processId);
      }
    } catch (RuntimeException e) {
      throw new UnexpectedRollbackException("Unable to remove equipment reference in process.", e);
    }
  }

}
