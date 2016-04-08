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
package cern.c2mon.client.ext.history;

import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import org.springframework.context.ApplicationContext;

import cern.c2mon.client.core.C2monServiceGateway;

public class C2monHistoryGateway {

  private static C2monHistoryManager historyManager = null;

  private static AlarmHistoryService alarmHistoryService = null;

  public static ApplicationContext context;

  private C2monHistoryGateway() {}

  /**
   * Initializes the C2monHistoryManager.
   */
  public static synchronized void initialize() {
    if (C2monServiceGateway.getApplicationContext() == null) {
      C2monServiceGateway.startC2monClientSynchronous();
      context = C2monServiceGateway.getApplicationContext();
      historyManager = context.getBean(C2monHistoryManager.class);
      alarmHistoryService = context.getBean(AlarmHistoryService.class);
    }
  }

 /**
  * @return the {@link C2monHistoryManager} instance
  */
  public static synchronized C2monHistoryManager getHistoryManager() {
    if (context == null) {
      initialize();
    }

    return historyManager;
  }

  /**
   * @return the {@link AlarmHistoryService} instance
   */
  public static synchronized AlarmHistoryService getAlarmHistoryService() {
    if (context == null) {
      initialize();
    }

    return alarmHistoryService;
  }
}
