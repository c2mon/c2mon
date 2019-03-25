/**
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
 */
package cern.c2mon.shared.client.serialize;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * Created by fritter on 23/03/16.
 */
@Data
@Builder
@AllArgsConstructor
public class TestUpdateTag implements ClientRequestResult{

  private Long tagId = null;

  private String valueClassName;

  private Object tagValue;

  private TagMode mode = null;

  private boolean simulated = false;

  @Singular
  private Collection<AlarmValueImpl> alarmValues = new ArrayList<AlarmValueImpl>();

  private DataTagQualityImpl tagQuality;

  private String description;

  /** The current tag value description */
  private String valueDescription;

  private Timestamp sourceTimestamp;

  private Timestamp daqTimestamp;

  private Timestamp serverTimestamp;

  public TestUpdateTag(){};
}
