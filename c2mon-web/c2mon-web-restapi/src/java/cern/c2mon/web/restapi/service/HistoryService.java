/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
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
 * @author Justin Lewis Salmon
 */
@Service
public class HistoryService {

  private static Logger logger = LoggerFactory.getLogger(HistoryService.class);

  /**
   *
   */
  private HistoryProvider historyProvider;

  /**
   *
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * @throws HistoryProviderException
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
   * @param id
   * @param type
   * @param records
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
   */
  public List<HistoryTagValueUpdate> getHistory(Long id, String type, int records) throws LoadingParameterException, UnknownResourceException {
    checkTagExistence(id, type);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setMaximumRecords(records);
    return getHistory(id, configuration);
  }

  /**
   * @param id
   * @param days
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
   */
  public List<HistoryTagValueUpdate> getHistory(Long id, String type, String days) throws LoadingParameterException, UnknownResourceException {
    checkTagExistence(id, type);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setNumberOfDays(Integer.valueOf(days));
    return getHistory(id, configuration);
  }

  /**
   * @param id
   * @param from
   * @param to
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
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
   *
   * @param id
   * @param configuration
   * @return
   * @throws LoadingParameterException
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
   *
   * @param id
   * @param type
   * @throws UnknownResourceException
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

    // This code should never be reached, as Spring handles the resource type within the controller
    else {
      throw new UnknownResourceException("Unknown resource type " + type);
    }
  }
}
