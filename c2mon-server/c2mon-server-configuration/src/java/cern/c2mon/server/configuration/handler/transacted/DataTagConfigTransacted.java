package cern.c2mon.server.configuration.handler.transacted;

import java.util.List;
import java.util.Properties;

import cern.c2mon.server.configuration.impl.ProcessChange;
import cern.tim.server.common.datatag.DataTag;
import cern.tim.shared.client.configuration.ConfigurationElement;
import cern.tim.shared.client.configuration.ConfigurationElementReport;

/**
 * For internal use only. Allows use of Spring AOP for transaction management.
 * 
 * @author Mark Brightwell
 *
 */
public interface DataTagConfigTransacted extends TagConfigTransacted<DataTag>{

  /**
   * Remove the DataTag within a transaction.
   * @param id id of DataTag to remove
   * @param elementReport report on success
   * @return the change event
   */
  List<ProcessChange> doRemoveDataTag(Long id, ConfigurationElementReport elementReport);
  
  /**
   * Creates a DataTag in the C2MON server.
   * 
   * @param element contains details of the Tag
   * @return creation event to send to the DAQ layer
   * @throws IllegalAccessException
   */
  ProcessChange doCreateDataTag(ConfigurationElement element) throws IllegalAccessException;
  
  /**
   * Updates a DataTag in the C2MON server. Always results in a event being
   * send to the DAQ layer.
   * @param id the id of the Tag to update
   * @param elementProperties details of the fields to modify
   * @return the change to send to the DAQ layer
   */
  ProcessChange doUpdateDataTag(Long id, Properties elementProperties);

}
