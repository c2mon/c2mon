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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

import static cern.c2mon.server.common.util.Java9Collections.setOfNonNulls;

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
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DataTagCacheObject extends AbstractInfoTagCacheObject implements DataTag {

  /**
   * Minimum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Comparable minValue = null;
  /**
   * Maximum value for range checks. If the system receives a tag value that is
   * less than the authorized minimum value, it will flag the new tag value as
   * invalid.
   */
  private Comparable maxValue = null;

  /**
   * Reference to equipment the Datatag is attached to.
   */
  private Long equipmentId = null;
  /**
   * Reference to sub equipment the DataTag is attached to.
   */
  private Long subEquipmentId = null;
  /**
   * Id of the Process this DataTag is attached to (loaded from DB also during cache loading).
   */
  private Long processId;

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

  public DataTagCacheObject(Long id) {
    super(id);
  }  @Override

  public Set<Long> getEquipmentIds() {
    return setOfNonNulls(equipmentId);
  }

  @Override
  public Set<Long> getProcessIds() {
    return setOfNonNulls(processId);
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    return setOfNonNulls(subEquipmentId);
  }
}
