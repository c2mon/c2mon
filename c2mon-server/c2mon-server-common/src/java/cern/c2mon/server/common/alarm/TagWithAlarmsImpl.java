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
