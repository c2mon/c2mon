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

import cern.c2mon.server.history.structure.TagRecord;

/**
 * Mapper interface for writing to and querying the
 * history of tags.
 *
 * @author Mark Brightwell
 */
public interface TagRecordMapper extends LoggerMapper<TagRecord> {

  /**
   * Removes all rows for a given tag from the table.
   * Only used for removing data inserted during testing on
   * the Oracle database.
   *
   * @param tagId remove all logs for a tag with this id
   */
  void deleteDataTagLog(Long tagId);

}
