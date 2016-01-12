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
package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.Collection;

import javax.jms.Destination;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.configuration.ConfigurationLoader;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestResult;


/**
 * Helper class for {@link ClientRequestDelegator} to handle
 * configuration requests.
 *
 * @author Matthias Braeger
 */
@Service
class ClientConfigurationRequestHandler {
  
  /** Private class logger */
  private static final Logger LOG = LoggerFactory.getLogger(ClientConfigurationRequestHandler.class);
  
  /**
   * Default TTL of replies to client requests
   */
  private static final long DEFAULT_REPLY_TTL = 5400000;
  
  /** Reference to the ConfigurationLoader */
  private final ConfigurationLoader configurationLoader;
  
  @Autowired
  public ClientConfigurationRequestHandler(final ConfigurationLoader configurationLoader) {
    this.configurationLoader = configurationLoader;
  }
  
  /**
   * Inner method which handles the Configuration Requests
   *
   * @param configurationRequest The configuration request sent from the client
   * @return Configuration Report
   */
  Collection<? extends ClientRequestResult> handleApplyConfigurationRequest(final ClientRequest configurationRequest,
                                                                            final Session session,
                                                                            final Destination replyDestination) {

    final Collection<ConfigurationReport> reports = new ArrayList<>(configurationRequest.getTagIds().size());

    // !!! TagId field is also used for Configuration Ids
    for (Long id : configurationRequest.getTagIds()) {

      final int configId = castLongToInt(id);

      switch (configurationRequest.getResultType()) {
      case TRANSFER_CONFIGURATION_REPORT:
        ClientRequestReportHandler reportHandler = new ClientRequestReportHandler(session, replyDestination, DEFAULT_REPLY_TTL);
        reports.add(configurationLoader.applyConfiguration(configId, reportHandler));
        if (LOG.isDebugEnabled()) {
          LOG.debug("Finished processing reconfiguration request with id " + configId);
        }
        break;
      default:
        LOG.error("handleConfigurationRequest() - Could not generate response message. Unknown enum ResultType " + configurationRequest.getResultType());
      }
    } // end while
    return reports;
  }

  /**
   * Inner method which handles a request to retrieve configuration reports
   *
   * @param configurationRequest The request sent by the client
   * @return A collection of configuration reports
   */
  Collection<? extends ClientRequestResult> handleRetrieveConfigurationsRequest(final ClientRequest configurationRequest,
                                                                                       final Session session,
                                                                                       final Destination replyDestination) {
    if (configurationRequest.getRequestParameter() != null) {
      return configurationLoader.getConfigurationReports(configurationRequest.getRequestParameter());
    } else {
      return configurationLoader.getConfigurationReports();
    }
  }
  
  /**
   * Helper method. Casts to an int. Logs a warning if the cast is unsafe.
   *
   * @param l long
   * @return int
   */
  private int castLongToInt(final long l) {

    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
      LOG.warn("castLongToInt() - unsafe cast of long " + l + " to int");
    }
    return (int) l;
  }
}
