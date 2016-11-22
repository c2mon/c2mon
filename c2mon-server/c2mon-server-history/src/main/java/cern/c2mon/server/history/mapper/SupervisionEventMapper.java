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
package cern.c2mon.server.history.mapper;

import java.util.List;

import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * Mapper for logging supervison info to the history database.
 *
 * @author Mark Brightwell
 */
public interface SupervisionEventMapper {

  /**
   * Log a supervision event in the supervision log table.
   *
   * @param supervisionEvent the event to log
   */
  void logSupervisionEvent(SupervisionEvent supervisionEvent);

  /**
   * Returns all supervision events for a given id (process or equipment)
   *
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
