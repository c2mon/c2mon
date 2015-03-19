/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2015 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.configviewer.statistics.stlog;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import cern.c2mon.shared.client.lifecycle.ServerLifecycleEvent;
import cern.c2mon.shared.client.supervision.SupervisionEvent;

/**
 * This class is a MyBatis mapper bean used to interface with the STL database
 * to get server lifecycle and process supervision events.
 *
 * <p>
 * Note: Other statistics, which rely on the c2mon-statistics-consumer being
 * configured, are accessed via
 * {@link cern.c2mon.statistics.generator.StatisticsMapper}.
 * </p>
 *
 * @author Justin Lewis Salmon
 */
public interface STLogStatisticsMapper {

  /**
   * Retrieve a list of {@link ServerLifecycleEvent} objects for a specified
   * time period.
   *
   * @param from the starting time
   * @param to the ending time
   *
   * @return a list of {@link ServerLifecycleEvent} objects within the given
   *         time period
   */
  List<ServerLifecycleEvent> getServerLifecycleEvents(@Param("from") final Timestamp from, @Param("to") final Timestamp to);

  /**
   * Retrieve a list of {@link SupervisionEvent} objects for a specified time
   * period for a given process.
   *
   * @param id the process id
   * @param from the starting time
   * @param to the ending time
   *
   * @return a list of {@link ServerLifecycleEvent} objects within the given
   *         time period
   */
  List<SupervisionEvent> getSupervisionEvents(@Param("id") final Long id, @Param("from") final Timestamp from, @Param("to") final Timestamp to);
}
