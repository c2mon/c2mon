/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.daqcommunication.out;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.common.NoSimpleValueParseException;
import cern.c2mon.shared.daq.command.CommandTag;
import cern.c2mon.shared.daq.config.Change;
import cern.c2mon.shared.daq.config.ConfigurationChangeEventReport;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueRequest;
import cern.c2mon.shared.daq.datatag.SourceDataTagValueResponse;
import cern.c2mon.shared.daq.exception.ProcessRequestException;

/**
 * Spring bean managing the sending of requests and commands to the DAQ layer,
 * and returning the response to the caller.
 * 
 * @author Mark Brightwell
 *
 */
public interface ProcessCommunicationManager {
  
  /**
   * Sends a command to the DAQ layer, waits for the response,
   * and returns the command report for sending back to the
   * client.
   * 
   * <p>The intention is for this method to be called by a Client request module on
   * reception of a command from the Client layer. 
   * 
   * @param commandTag the command to execute
   * @param value the value of the command
   * @param <T> the value type of the command, set before execution
   * @return the report providing feedback on the execution of the command, including a return value
   * @throws NullPointerException if either parameter is null
   * @throws CacheElementNotFoundException if the command to execute cannot be located in the cache
   */
  <T> CommandReport executeCommand(CommandTag<T> commandTag, T value);
  
  /**
   * Requests the latest values of the data tags from the DAQ (DAQ id specified in the request object).
   * The values returned are those held in the DAQ memory (DAQ core functionality) - no refresh
   * request is made to the equipment.
   * Throws a ProcessRequestException if the request was unsuccessful (unchecked).
   * @param pRequest the request details (type: process, equipment, datatag; id of the element) 
   * @return the response containing the latest source values
   */
  SourceDataTagValueResponse requestDataTagValues(SourceDataTagValueRequest pRequest) throws ProcessRequestException;
  
  /**
   * Sends a list of configuration changes to be applied on the DAQ layer
   * and returns a report with details of the success/failure of each.
   * 
   * <p>Never returns null, but throw {@link RuntimeException} if DAQ does
   * not reply.
   * 
   * @param processId the process these changes need sending to
   * @param changeList the changes to be applied
   * @return a report from the Process
   * @throws TransformerException 
   * @throws InstantiationException 
   * @throws IllegalAccessException 
   * @throws ParserConfigurationException 
   * @throws NoSimpleValueParseException 
   * @throws NoSuchFieldException 
   */
  ConfigurationChangeEventReport sendConfiguration(Long processId, List<Change> changeList) 
                        throws ParserConfigurationException, IllegalAccessException, InstantiationException, TransformerException, NoSuchFieldException, NoSimpleValueParseException; 
  
}
