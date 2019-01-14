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
package cern.c2mon.server.cache.alarm.impl;

import cern.c2mon.server.cache.C2monCacheListener;
import cern.c2mon.server.common.tag.Tag;

/**
 * Alternative listener that simply evaluates the alarms associated
 * to updated tags. Listeners can then register to the Alarm cache
 * to receive alarm notifications.
 * 
 * <p>Should not be used in conjunction with the AlarmAggregator, or
 * the alarms will be evaluated twice.
 *
 * TODO not implemented yet as TIM will use the Aggregator design
 * 
 * @author Mark Brightwell
 *
 */
class AlarmEvaluator implements C2monCacheListener<Tag> {
  
  //TODO register to alarm cache

  @Override
  public void notifyElementUpdated(final Tag object) {
    // TODO call alarm evaluation in alarm facade
  }

  @Override
  public void confirmStatus(Tag cacheable) {
    // TODO Auto-generated method stub
  }
}
