/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.configviewer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.common.process.ProcessConfiguration;

/**
 * ProcessService providing the XML representation for a given process.
 */
@Service
public class ProcessService {

  /**
   * ProcessService logger
   */
  private static Logger logger = LoggerFactory.getLogger(ProcessService.class);

  /**
   * Gateway to ConfigLoaderService
   */
  @Autowired
  private ServiceGateway gateway;


  /**
   * Gets the XML representation of the process
   * @param processName processName
   * @return XML
   * @throws Exception if id not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public String getProcessXml(final String processName) throws Exception {

    try {
      String  xml = getXml(processName);
      if (xml != null)
        return xml;
      else
        throw new TagIdException("No luck. Try another processName.");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid processName");
    }
  }

  /**
   * Gets all available process names
   * @return a collection of all available process names
   */
  public Collection<String> getProcessNames() {

    Collection <ProcessNameResponse> processNames = gateway.getTagManager().getProcessNames();
    Collection <String> names = new ArrayList<String>();

    Iterator<ProcessNameResponse> i = processNames.iterator();

    while (i.hasNext()) {

      ProcessNameResponse p = i.next();
      names.add(p.getProcessName());
    }
    return names;
  }

  public ProcessConfiguration getProcessConfiguration(final String processName) throws Exception {
    String xml = getXml(processName);
    Serializer serializer = new Persister();
    ProcessConfiguration processConfiguration = serializer.read(ProcessConfiguration.class, xml);
    return processConfiguration;
  }


  /**
   * Private helper method. Gets the XML representation of the process
   * @param processName processName
   * @return XML
   */
  private String getXml(final String processName) {

    String xml = gateway.getTagManager().getProcessXml(processName);

    logger.debug("getXml fetch for process " + processName + ": "
        + (xml == null ? "NULL" : "SUCCESS"));

    return xml;
  }
}
