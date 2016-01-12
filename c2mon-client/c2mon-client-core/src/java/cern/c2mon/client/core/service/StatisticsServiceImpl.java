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
package cern.c2mon.client.core.service;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.StatisticsService;
import cern.c2mon.client.jms.RequestHandler;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class StatisticsServiceImpl implements StatisticsService {
  
  /** Provides methods for requesting tag information from the C2MON server */
  private final RequestHandler clientRequestHandler;
  

  /**
   * Default Constructor, used by Spring to instantiate the Singleton service
   *
   * @param requestHandler Provides methods for requesting tag information from the C2MON server
   */
  @Autowired
  protected StatisticsServiceImpl(final RequestHandler requestHandler) {
    this.clientRequestHandler = requestHandler;
  }
  
  @Override
  public TagStatisticsResponse getTagStatistics() {
    try {
      TagStatisticsResponse response = clientRequestHandler.requestTagStatistics();
      return response;
    } catch (JMSException e) {
      log.error("getConfigurationReports() - JMS connection lost -> Could not retrieve configuration reports from the C2MON server.", e);
    }

    return null;
  }
}
