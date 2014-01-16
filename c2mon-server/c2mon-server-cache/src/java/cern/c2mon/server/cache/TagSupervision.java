/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache;

import java.sql.Timestamp;

import cern.c2mon.server.common.tag.Tag;

/**
 * Interface of bean used for modifying a Tag object to
 * take into account the Supervision status. Should be 
 * used for rule and datatags by cache listeners. The
 * Tag parameter in all the methods below is modified.
 * 
 * <p>The methods will modify the quality of the tag object
 * appropriately. Listeners are not notified.
 * 
 * <p>IMPORTANT: do not use for updating objects that are
 * living in the cache. The facade beans should be used
 * for that purpose.
 * 
 * @author Mark Brightwell
 *
 */
public interface TagSupervision {

  /**
   * Call when a DAQ is detected as DOWN.
   * 
   * @param tag the tag object to modify
   * @param message the quality message
   * @param timestamp the new timestamp (becomes cache timestamp)
   */
  void onProcessDown(Tag tag, String message, Timestamp timestamp);
  /**
   * Call when a DAQ is detected as UP.
   * 
   * @param tag the tag object to modify
   * @param message the quality message
   * @param timestamp the new timestamp (becomes cache timestamp)
   */
  void onProcessUp(Tag tag, String message, Timestamp timestamp);
  
  /**
   * Modifies Tag object appropriately when equipment is
   * detected as down.
   * 
   * @param tag the tag object to modify
   * @param message the quality message
   * @param timestamp the new timestamp (becomes cache timestamp)
   */
  void onEquipmentDown(Tag tag, String message, Timestamp timestamp);
  
  /**
   * Modifies Tag object appropriately when equipment is
   * detected as up.
   * 
   * @param tag the tag object to modify
   * @param message the quality message
   * @param timestamp the new timestamp (becomes cache timestamp)
   */
  void onEquipmentUp(Tag tag, String message, Timestamp timestamp);
  
}
