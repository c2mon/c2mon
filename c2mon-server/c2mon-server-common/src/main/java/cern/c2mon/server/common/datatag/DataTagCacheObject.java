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
package cern.c2mon.server.common.datatag;

import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

/**
 * Represents the data tag objects stored in the cache. These contain both the
 * configuration information and the current value. The persistence module keeps
 * a copy of the current cache value in the database. DataTag objects correspond
 * to values arriving from the DAQ layer.
 *
 * <p>Queries to the cache are guaranteed to return a non-null object and throw an
 * exception if no object is found.
 *
 * @author Mark Brightwell
 */
public class DataTagCacheObject extends AbstractInfoTagCacheObject implements DataTag {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minor changes to the class do not prevent us from
   * reading back DataTagCacheObjects we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -12345679L;

  public DataTagCacheObject(Long id, String name, String datatype, short mode) {
    super(id, name, datatype, mode);
    setName(name);
    setDataType(datatype);
    setMode(mode);
    setDataTagQuality(new DataTagQualityImpl());
  }

  public DataTagCacheObject(long id) {
    super(id);
  }
}
