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
package cern.c2mon.server.cache.loading;

import cern.c2mon.server.common.process.Process;

/**
 * Process DAO specification.
 *
 * @author Mark Brightwell
 */
public interface ProcessDAO extends CacheLoaderDAO<Process>, ConfigurableDAO<Process> {

  void deleteProcess(Long processId);

  /**
   * Retrieve the number of tags currently configured for a given process.
   *
   * @param processId the ID of the process
   *
   * @return the number of tags configured for the process
   */
  Integer getNumTags(Long processId);

  /**
   * Retrieve the number of currently configured tags that are invalid for a
   * process.
   *
   * @param processId the ID of the process
   *
   * @return the number of invalid tags configured for the process
   */
  Integer getNumInvalidTags(Long processId);

  Long getIdByName(String name);

}
