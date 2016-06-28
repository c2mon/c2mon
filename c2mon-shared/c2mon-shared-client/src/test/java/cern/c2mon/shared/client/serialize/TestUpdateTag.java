/*
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
 */

package java.client.serialize;

import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by fritter on 23/03/16.
 */
@Data
@Builder
@AllArgsConstructor
public class TestUpdateTag implements ClientRequestResult{

  @NotNull
  @Min(1)
  private Long tagId = null;

  private String valueClassName;

  private Object tagValue;

  @NotNull
  private TagMode mode = null;

  private boolean simulated = false;

  @Singular
  private Collection<AlarmValueImpl> alarmValues = new ArrayList<AlarmValueImpl>();

  @NotNull
  private DataTagQualityImpl tagQuality;

  private String description;

  /** The current tag value description */
  private String valueDescription;

  @Past
  private Timestamp sourceTimestamp;

  @Past
  private Timestamp daqTimestamp;

  @NotNull @Past
  private Timestamp serverTimestamp;

  public TestUpdateTag(){};
}
