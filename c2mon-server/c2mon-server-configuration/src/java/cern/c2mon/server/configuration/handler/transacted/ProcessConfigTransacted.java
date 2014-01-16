package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import org.springframework.transaction.UnexpectedRollbackException;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.c2mon.shared.client.configuration.ConfigurationElement;
import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.server.common.process.Process;

/**
 *  For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface ProcessConfigTransacted {

  /**
   * Transacted method creating a Process
   * @param element config details
   * @return event for DAQ
   * @throws IllegalAccessException
   */
  ProcessChange doCreateProcess(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Transacted method updating a Process.
   * @param id of Process
   * @param properties update details
   * @return event for DAQ
   * @throws IllegalAccessException
   */
  ProcessChange doUpdateProcess(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Transacted method for removing a Process.
   * @param process ref to Process
   * @param processReport report with details of success/failure
   * @return event for DAQ (in this case not sent down, just for indicating restart/stop)
   */
  ProcessChange doRemoveProcess(Process process, ConfigurationElementReport processReport);

  /**
   * Removes an equipment reference from the process that contains it.
   * @param equipmentId the equipment to remove
   * @param processId the process to remove the equipment reference from
   * @throws UnexpectedRollbackException if this operation fails
   */
  void removeEquipmentFromProcess(Long equipmentId, Long processId);  
  
}
