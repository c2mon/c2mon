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


import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;


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
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class DataTagCacheObject extends AbstractTagCacheObject implements DataTag {

  /**
   * Version number of the class used during serialization/deserialization. This
   * is to ensure that minore changes to the class do not prevent us from
   * reading back DataTagCacheObjects we have serialized earlier. If fields are
   * added/removed from the class, the version number needs to change.
   */
  private static final long serialVersionUID = -12345678L;

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
   * Address configuration of the datatag (if any)
   */
  private DataTagAddress address = null;


  /**
   * Timestamp set by the source for the current value (may be null).
   */
  private Timestamp sourceTimestamp;

  /**
   * Timestamp set by the DAQ for the current value (may be null).
   * Is set when the value is updated or invalidated in the DAQ core map.
   */
  private Timestamp daqTimestamp;

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
   * Constructor used to return a cache object when the object cannot be found
   * in the cache. Sets the quality to UNINITIALISED with the message
   * "No value received for this data tag so far".
   *
   * The fields sets in this constructor (4 parameters + quality field) can be
   * assumed to always be non-null on objects circulating in the server. All other
   * fields may take on null values and appropriate checks should be made.
   */
  public DataTagCacheObject(Long id, String name, String datatype, short mode) {
    super(id);
    setName(name);
    setDataType(datatype);
    setMode(mode);
    setDataTagQuality(new DataTagQualityImpl());
  }

  //for testing only so far
  public DataTagCacheObject(final Long id) {
    super(id);
  }

  /**
   * Clone implementation.
   * @throws CloneNotSupportedException
   */
  @Override
  public DataTagCacheObject clone() throws CloneNotSupportedException {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) super.clone();
    if (address != null) {
      dataTagCacheObject.address = (DataTagAddress) this.address.clone();
    }
    if (sourceTimestamp != null) {
      dataTagCacheObject.sourceTimestamp = (Timestamp) this.sourceTimestamp.clone();
    }
    return dataTagCacheObject;
  }

  /**
   * @param serverTimestamp the serverTimestamp to set
   */
  public void setSourceTimestamp(Timestamp serverTimestamp) {
    if (this.sourceTimestamp == null || serverTimestamp == null){
      this.sourceTimestamp = serverTimestamp;
    } else {
      this.sourceTimestamp.setTime(serverTimestamp.getTime());
    }
  }

  public final boolean hasAddress() {
    return this.address != null;
  }

  @Override
  public final Timestamp getTimestamp() {
    if (sourceTimestamp != null) {
      return sourceTimestamp;
    } else if (daqTimestamp != null){
      return daqTimestamp;
    } else {
      return getCacheTimestamp();
    }
  }

  /**
   * @param daqTimestamp the daqTimestamp to set
   */
  public void setDaqTimestamp(Timestamp daqTimestamp) {
    if (this.daqTimestamp == null || daqTimestamp == null){
      this.daqTimestamp = daqTimestamp;
    } else {
      this.daqTimestamp.setTime(daqTimestamp.getTime());
    }
  }

  @Override
  public Set<Long> getEquipmentIds() {
    Set<Long> returnSet = new HashSet<Long>();
    if (equipmentId != null) {
      returnSet.add(equipmentId);
    }
    return returnSet;
  }

  @Override
  public Set<Long> getProcessIds() {
    Set<Long> returnSet = new HashSet<Long>();
    if (processId != null) {
      returnSet.add(processId);
    }
    return returnSet;
  }

  @Override
  public Set<Long> getSubEquipmentIds() {
    Set<Long> returnSet = new HashSet<Long>();
    if (subEquipmentId != null) {
      returnSet.add(subEquipmentId);
    }
    return returnSet;
  }
}
