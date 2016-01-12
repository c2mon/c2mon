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
package cern.c2mon.web.restapi.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.ext.history.C2monHistoryGateway;
import cern.c2mon.client.ext.history.common.HistoryLoadingConfiguration;
import cern.c2mon.client.ext.history.common.HistoryLoadingManager;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.restapi.exception.UnknownResourceException;

/**
 * Service bean for accessing historical data for supported resources from the
 * C2MON server.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class HistoryService {

  private static Logger logger = LoggerFactory.getLogger(HistoryService.class);

  /**
   * Reference to the history provider.
   */
  private HistoryProvider historyProvider;

  /**
   * Reference to the service gateway bean.
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Constructor.
   *
   * @throws HistoryProviderException if an error occurred while retrieving the
   *           {@link HistoryProvider}.
   */
  public HistoryService() throws HistoryProviderException {
    try {
      historyProvider = C2monHistoryGateway.getHistoryManager().getHistoryProviderFactory().createHistoryProvider();

    } catch (HistoryProviderException e) {
      logger.error("Error instantiating HistoryService", e);
      throw new HistoryProviderException("Cannot retrieve historical data because no history provider is accessible.");
    }
  }

  /**
   * Retrieve a specific number of records of historical data for a specific
   * resource.
   *
   * @param id the ID of the resource
   * @param type the type of the resource (datatags, alarms, etc.)
   * @param records the number of records to retrieve
   *
   * @return the list containing the specified number (or less) of historical
   *         records
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  public List<HistoryTagValueUpdate> getHistory(Long id, String type, int records) throws LoadingParameterException, UnknownResourceException {
    checkTagExistence(id, type);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setMaximumRecords(records);
    return getHistory(id, configuration);
  }

  /**
   * Retrieve all historical records for a specific number of days for a
   * specific resource.
   *
   * @param id the ID of the resource
   * @param type the type of the resource (datatags, alarms, etc.)
   * @param days the number of days to retrieve
   *
   * @return a list of {@link HistoryTagValueUpdate} objects
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  public List<HistoryTagValueUpdate> getHistory(Long id, String type, String days) throws LoadingParameterException, UnknownResourceException {
    checkTagExistence(id, type);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setNumberOfDays(Integer.valueOf(days));
    return getHistory(id, configuration);
  }

  /**
   * Retrieve all historical records of a resource between two dates for a
   * specific resource.
   *
   * @param id the ID of the resource
   * @param type the type of the resource (datatags, alarms, etc.)
   * @param from the start date
   * @param to the end date
   *
   * @return a list of {@link HistoryTagValueUpdate} objects
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  public List<HistoryTagValueUpdate> getHistory(Long id, String type, Date from, Date to) throws LoadingParameterException, UnknownResourceException {
    checkTagExistence(id, type);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setStartTime(new Timestamp(from.getTime()));
    configuration.setEndTime(new Timestamp(to.getTime()));
    return getHistory(id, configuration);
  }

  /**
   * Retrieve historical data from the {@link HistoryProvider}.
   *
   * @param id the ID of the resource to retrieve history for
   * @param configuration the configuration parameter object used to specify
   *          what to load
   *
   * @return a list of {@link HistoryTagValueUpdate} objects
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   */
  private List<HistoryTagValueUpdate> getHistory(Long id, HistoryLoadingConfiguration configuration) throws LoadingParameterException {
    final HistoryLoadingManager loadingManager = C2monHistoryGateway.getHistoryManager().createHistoryLoadingManager(historyProvider, Arrays.asList(id));

    try {
      loadingManager.setConfiguration(configuration);
      loadingManager.beginLoading(false);

    } catch (LoadingParameterException e) {
      logger.error("Error loading history", e);
      throw e;
    }

    return new ArrayList<>(loadingManager.getAllHistoryConverted(id));
  }

  /**
   * Check that a particular tag of a given type exists.
   *
   * @param id the ID of the tag
   * @param type the type of the tag
   *
   * @throws UnknownResourceException if no tag was found with the given ID
   */
  private void checkTagExistence(final Long id, final String type) throws UnknownResourceException {

    if (type.equals("datatags")) {
      Collection<ClientDataTagValue> tags = gateway.getTagManager().getDataTags(Arrays.asList(id));

      if (tags.isEmpty()) {
        throw new UnknownResourceException("No datatag was found with id " + id);
      }
    }

    else if (type.equals("alarms")) {
      Collection<AlarmValue> alarms = gateway.getTagManager().getAlarms(Arrays.asList(id));

      if (alarms.isEmpty()) {
        throw new UnknownResourceException("No alarm was found with id " + id);
      }
    }

    // This code should never be reached, as Spring handles the resource type
    // within the controller
    else {
      throw new UnknownResourceException("Unknown resource type " + type);
    }
  }
}
