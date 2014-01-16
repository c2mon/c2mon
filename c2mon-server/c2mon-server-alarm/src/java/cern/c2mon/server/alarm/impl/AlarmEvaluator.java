/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.alarm.impl;

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
