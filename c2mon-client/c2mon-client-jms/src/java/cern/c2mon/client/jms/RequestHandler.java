/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.jms;

import java.util.Collection;

import javax.jms.JMSException;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.tim.shared.client.command.CommandExecuteRequest;
import cern.tim.shared.client.command.CommandReport;
import cern.tim.shared.client.command.CommandTagHandle;
import cern.tim.shared.client.configuration.ConfigurationReport;

/**
 * Interface to Spring singleton bean proving convenient server
 * request methods.
 * 
 * @author Mark Brightwell
 *
 */
public interface RequestHandler {
    

    /**
     * Queries the server for the latest values and configuration
     * details for the request tags.
     * 
     * <p>If called with an empty collection returns an empty collection.
     * 
     * @param configurationId the id of the configuration report
     * @return a Configuration Report
     */
    ConfigurationReport applyConfiguration(Long configurationId);    
    
    /**
     * Queries the server for the latest values and configuration
     * details for the request tags.
     * 
     * <p>If called with an empty collection returns an empty collection.
     * 
     * @param tagIds the ids of the tags
     * @return a collection of TagConfigurations
     * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
     * @throws NullPointerException if called with a null argument
     * @throws RuntimeException if the response from the server is null (probable timeout)
     */
    Collection<AlarmValue> requestAlarms(Collection<Long> tagIds) throws JMSException;  
    
    /**
     * Queries the server for the latest values and configuration
     * details for the request tags.
     * 
     * <p>If called with an empty collection returns an empty collection.
     * 
     * @param tagIds the ids of the tags
     * @return a collection of TagConfigurations
     * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
     * @throws NullPointerException if called with a null argument
     * @throws RuntimeException if the response from the server is null (probable timeout)
     */
    Collection<TagConfig> requestTagConfigurations(Collection<Long> tagIds) throws JMSException;    
    

  /**
   * Queries the server for the latest values and configuration
   * details for the request tags.
   * 
   * <p>If called with an empty collection returns an empty collection.
   * 
   * @param tagIds the ids of the tags
   * @return a collection of transfer objects with the values/configuration information
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   * @throws NullPointerException if called with a null argument
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<TagUpdate> requestTags(Collection<Long> tagIds) throws JMSException;
  
  /**
   * Queries the server for the latest values for the request tags.
   * 
   * <p>If called with an empty collection returns an empty collection.
   * 
   * @param tagIds the ids of the tags
   * @return a collection of transfer objects with the value information
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request
   * @throws NullPointerException if called with a null argument
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<TagValueUpdate> requestTagValues(Collection<Long> tagIds) throws JMSException;
  
  /**
   * Queries the server for the current Supervision status of all
   * entities in the server (Process, Equipment and SubEquipment).
   * @return a collection of current events, each containing the status
   *                  of one of the entities
   * @throws JMSException if not currently connected or if a JMS problem occurs while making the request 
   * @throws RuntimeException if the response from the server is null (probable timeout)
   */
  Collection<SupervisionEvent> getCurrentSupervisionStatus() throws JMSException;
  
  /**
   * Request CommandTags from the server.
   * @param commandIds ids of desired command tags
   * @return a collection of command handle objects
   * @throws JMSException if a JMS problems occurs or if not connected at the moment
   * @throws RuntimeException if no response is received from the server (probably timeout)
   */
  Collection<CommandTagHandle> requestCommandTagHandles(Collection<Long> commandIds);
  
  /**
   * 
   * @param <T> the value type of the command
   * @param commandExecuteRequest the request details for executing this command
   * @return a report about this execution
   * @throws JMSException if a JMS problems occurs or if not connected at the moment
   * @throws RuntimeException if no response is received from the server (probably timeout)
   */
  <T> CommandReport executeCommand(CommandExecuteRequest<T> commandExecuteRequest) throws JMSException;
  
  /**
   * Requests the DAQ config XML for a given process. Never returns null.
   * @param processName the name of the Process
   * @return the DAQ XML as String 
   * @throws JMSException if JMS problem occurs or not connected at the moment
   * @throws RuntimeException if server is unable to answer the request (message contains details)
   */
  String getProcessXml(String processName) throws JMSException;

  /**
   * Requests a list of Names for all the existing processes.
   * 
   * @return a list of all process names 
   * @throws JMSException if JMS problem occurs or not connected at the moment
   */      
  Collection<ProcessNameResponse> getProcessNames() throws JMSException;
}
