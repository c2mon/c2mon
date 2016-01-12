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

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.restapi.exception.UnknownResourceException;

/**
 * Service bean for accessing {@link AlarmValue} objects from the C2MON server.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class AlarmService {

  /**
   * Reference to the service gateway bean.
   */
  @Autowired
  private ServiceGateway gateway;

  /**
   * Retrieve a {@link AlarmValue} object.
   *
   * @param id the ID of the {@link AlarmValue} to retrieve
   * @return the {@link AlarmValue} object
   *
   * @throws UnknownResourceException if no alarm could be found with the given
   *           ID
   */
  public AlarmValue getAlarmValue(Long id) throws UnknownResourceException {
    List<AlarmValue> list = (List<AlarmValue>) gateway.getTagManager().getAlarms(Arrays.asList(id));

    if (list.isEmpty()) {
      throw new UnknownResourceException("No alarm with id " + id + " was found.");
    } else {
      return list.get(0);
    }
  }
}
