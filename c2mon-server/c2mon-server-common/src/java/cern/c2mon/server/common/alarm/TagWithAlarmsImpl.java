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
package cern.c2mon.server.common.alarm;

import java.util.Collection;

import cern.c2mon.server.common.tag.Tag;

/**
 * Standard implementation of the TagWithAlarms interface.
 * Use the interface!
 * 
 * @author Mark Brightwell
 *
 */
public class TagWithAlarmsImpl implements TagWithAlarms {

  /**
   * A tag.
   */
  private Tag tag;
  
  /**
   * Alarms associated to this tag.
   * 
   * <p>(the intention is that they are evaluated according
   * to the Tag value in the Tag field).
   */
  private Collection<Alarm> alarms;
    
  /**
   * Only constructor.
   * @param tag the Tag
   * @param alarms the associated Alarms
   */
  public TagWithAlarmsImpl(final Tag tag, final Collection<Alarm> alarms) {
    super();
    this.tag = tag;
    this.alarms = alarms;
  }

  @Override
  public Collection<Alarm> getAlarms() {
    return alarms;
  }

  @Override
  public Tag getTag() {
    return tag;
  }

}
