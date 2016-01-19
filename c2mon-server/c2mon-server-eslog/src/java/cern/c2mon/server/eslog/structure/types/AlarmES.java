/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.structure.types;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Represents an Alarm Event for ElasticSearch.
 * @author Alban Marguet
 */
@Slf4j
@Data
public class AlarmES {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private long tagId;
  private long alarmId;

  private String faultFamily;
  private String faultMember;
  private int faultCode;

  private boolean active;
  private int priority;
  private String info;

  private long serverTimestamp;
  private String timezone;

  @Override
  public String toString() {
    return GSON.toJson(this);
  }
}