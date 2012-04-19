package cern.c2mon.server.configuration.handler.transacted;

import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.common.control.ControlTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface ControlTagConfigTransacted extends TagConfigTransacted<ControlTag> {

  /**
   * Transacted method creating a control tag.
   * @param element configuration details for creation
   * @throws IllegalAccessException
   * @return change object for sending to DAQ
   */
  ProcessChange doCreateControlTag(ConfigurationElement element) throws IllegalAccessException;

  /**
   * Transacted method updating a control tag.
   * @param id of tag to update
   * @param properties with update info
   * @throws IllegalAccessException
   * @return change object for sending to DAQ
   */
  ProcessChange doUpdateControlTag(Long id, Properties properties) throws IllegalAccessException;

  /**
   * Transacted method removing a control tag. Need to confirm cache removal once
   * this returns.
   * @param id of tag to remove
   * @param elementReport report on removal
   * @return change object for sending to DAQ
   */
  ProcessChange doRemoveControlTag(Long id, ConfigurationElementReport elementReport);

  
  /**
   * Simple wrapped method call, not transacted. See ControlTagConfigHandler docs.
   * @param configId
   * @param controlTagId
   * @param equipmentId
   * @param processId
   * @return
   */
  ProcessChange getCreateEvent(Long configId, Long controlTagId, Long equipmentId, Long processId);

}
