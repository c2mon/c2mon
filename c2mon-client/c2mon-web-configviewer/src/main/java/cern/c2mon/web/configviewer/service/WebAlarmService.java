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
import java.util.List;

import cern.c2mon.client.core.manager.TagManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

/**
 * Alarm service providing the XML representation of a given alarm
 */
@Service
public class WebAlarmService {

  /**
   * AlarmService logger
   */
  private static Logger logger = LoggerFactory.getLogger(WebAlarmService.class);

  /**
   * Gateway to C2monService
   */
  @Autowired
  private TagManager tagManager;

  /**
   * Gets the XML representation of the current value and configuration of an
   * alarm
   *
   * @param alarmId id of the alarm
   *
   * @return XML representation of alarm value and configuration
   *
   * @throws TagIdException if alarm was not found or a non-numeric id was
   *           requested ({@link TagIdException}), or any other exception thrown
   *           by the underlying service gateway.
   */
  public String getAlarmTagXml(final String alarmId) throws TagIdException {
    try {
      AlarmValueImpl alarm = (AlarmValueImpl) getAlarmValue(Long.parseLong(alarmId));
      if (alarm != null)
        return alarm.getXml();
      else
        throw new TagIdException("No alarm found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid alarm id");
    }
  }

  /**
   * Retrieves a alarmValue object from the service gateway tagManager
   *
   * @param alarmId id of the alarm
   *
   * @return alarm value
   */
  public AlarmValue getAlarmValue(final long alarmId) {
    AlarmValue av = null;
    List<Long> alarmIds = new ArrayList<Long>();
    alarmIds.add(alarmId);
    Collection<AlarmValue> alarms = tagManager.getAlarms(alarmIds);
    // tagManager.getAlarms(alarmIds);
    Iterator<AlarmValue> it = alarms.iterator();
    if (it.hasNext()) {
      av = it.next();
    }
    logger.debug("Alarm fetch for alarm " + alarmId + ": " + (av == null ? "NULL" : "SUCCESS"));
    return av;
  }

}
