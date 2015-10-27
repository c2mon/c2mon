/*******************************************************************************
 * This file is part of the C2MON project.
 * See http://cern.ch/c2mon
 *
 * Copyright (C) 2004 - 2015 CERN. 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: C2MON team, c2mon-support@cern.ch
 ******************************************************************************/
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.ProcessXMLProvider;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.process.ProcessNameResponseImpl;
import cern.c2mon.shared.client.process.ProcessXmlResponse;
import cern.c2mon.shared.client.process.ProcessXmlResponseImpl;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;

/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * DAQ process related requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientProcessRequestHandler {
  
  /** Private class logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientProcessRequestHandler.class);

  /**
   * Reference to the Process cache that provides a list of all the process
   * names
   */
  private final ProcessCache processCache;
  
  /** Ref to the the bean providing DAQ XML */
  private final ProcessXMLProvider processXMLProvider;
 

  @Autowired
  public ClientProcessRequestHandler(final ProcessCache processCache,
                               final ProcessXMLProvider processXMLProvider) {
    this.processCache = processCache;
    this.processXMLProvider = processXMLProvider;
  }

  /**
   * Inner method which handles the process names request
   *
   * @param clientRequest A process name sent from the client
   * @return a Collection of all available process names
   */
  Collection<? extends ClientRequestResult> handleProcessNamesRequest(final ClientRequest clientRequest) {

    Collection<ProcessNameResponse> names = new ArrayList<ProcessNameResponse>();

    for (Long processId : processCache.getKeys()) {
      cern.c2mon.server.common.process.Process process = processCache.get(processId);
      names.add(new ProcessNameResponseImpl(process.getName()));
    }
    return names;
  }

  /**
   * Inner method which handles the Daq Xml Requests
   *
   * @param daqXmlRequest The daq Xml Request sent from the client
   * @return a ProcessXmlResponse
   */
  Collection<? extends ClientRequestResult> handleDaqXmlRequest(final ClientRequest daqXmlRequest) {

    Collection<ProcessXmlResponse> singleXML = new ArrayList<ProcessXmlResponse>(1);
    ProcessXmlResponseImpl processXmlResponse;
    try {
      String xmlString = processXMLProvider.getProcessConfigXML(daqXmlRequest.getRequestParameter());
      processXmlResponse = new ProcessXmlResponseImpl();
      processXmlResponse.setProcessXML(xmlString);
    } catch (CacheElementNotFoundException cacheEx) {
      String errorMessage = "Error while getting Process configruation:" + cacheEx.getMessage();
      LOG.warn(errorMessage, cacheEx);
      processXmlResponse = new ProcessXmlResponseImpl(false, errorMessage);
    }
    singleXML.add(processXmlResponse);
    return singleXML;
  }
}
