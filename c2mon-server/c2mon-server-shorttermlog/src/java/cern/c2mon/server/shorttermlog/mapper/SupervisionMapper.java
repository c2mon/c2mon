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
package cern.c2mon.server.shorttermlog.mapper;

import java.util.List;

import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * iBatis mapper for logging supervison info
 * to the STL DB account
 * 
 * @author Mark Brightwell
 *
 */
public interface SupervisionMapper {

  /**
   * Log a supervision event in the supervision log table.
   * 
   * @param supervisionEvent the event to log
   */
  void logSupervisionEvent(SupervisionEvent supervisionEvent);
  
  /**
   * Returns all supervision events for a given id (process or equipment)
   * @param id the id of the entity (not nec. unique across entities!)
   * @return a list of supervison events
   */
  List<SupervisionEvent> getEntitySupervision(Long id);

  /**
   * Deletes all entries for entities with the given id  (no restriction on entity type)
   * 
   * @param id of the entity for which the rows should be deleted
   */
  void testDelete(Long id);
  
  
}
