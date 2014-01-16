/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.datatag;


import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.DataTagAddress;
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
 *
 */
public class DataTagCacheObject extends AbstractTagCacheObject implements DataTag, Cacheable, Cloneable {
   
   /** 
    * Log4j logger instance 
    **/
   private static final Logger LOGGER = Logger.getLogger(DataTagCacheObject.class);

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
  
  /**
   * Default constructor 
   */
  public DataTagCacheObject() {
      super();
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
   * Constructor This constructor should only be used to create a "fake"
   * DataTagCacheObject representing a tag that does not exist within the TIM
   * system. When a client requests a value for a tag ID that doesn't exist,
   * such a fake object will be return. The DataTagQuality of this tag will
   * clearly indicate that the tag is non-existent.
   * 
   * @param id
   *          identifier of the non-existing tag.
   */
//  public DataTagCacheObject(final Long pId) {
//    this(
//      pId,                     // ID
//      "UNKNOWN",               // name
//      "UNKNOWN",               // description
//      "UNKNOWN",               // data type
//      null,                    // value dictionary
//      false,                   // control tag
//      MODE_OPERATIONAL,        // mode
//      false,                   // logged
//      "N/A",                   // unit
//      null,                    // minimum value
//      null,                    // maximum value
//      null,                    // address
//      null,                    // rule text
//      null,                    // dip address
//      null,                    // JAPC address
//      null,                    // value
//      "N/A",                   // value description
//      new Timestamp(0),        // timestamp
//      new Timestamp(0),        // server timestamp
//      new DataTagQuality(),    // quality
//      false,                   // simulated flag
//      null,                    // dependent alarms
//      null                     // dependent rules
//    );
//    invalidate(DataTagQuality.INVALID_TAG,
//        "Tag identifier not known to the system.",
//        new Timestamp(System.currentTimeMillis()));
//  }

  
  /**
   * TEMPORARY IMPLEMENTATION OF EQUALS (USED IN TESTING ONLY SO FAR)
   */
//  @Override
//  public boolean equals(Object object) {
//    return ((DataTagCacheObject) object).getValue() == this.getValue();
//  }
  
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
  
  public final Long getEquipmentId() {
    return this.equipmentId;
  }
  
  /**
   * Set the equipment Id for this DataTag. 
   * @param pEquipmentId
   */
  public final void setEquipmentId (final Long pEquipmentId) {
    this.equipmentId = pEquipmentId;
  }

  @Override
  public final Comparable getMinValue() {
    return this.minValue;
  }
  
  @Override
  public final Comparable getMaxValue() {
    return this.maxValue;
  }

  public final boolean hasAddress() {
    return this.address != null;
  }

  @Override
  public final DataTagAddress getAddress() {
    return this.address;
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
  
  @Override
  public final Timestamp getSourceTimestamp() {
    return this.sourceTimestamp;
  }

    
//  public final String toString() {
//    StringBuffer str = new StringBuffer("<DataTag ");
//
//    str.append("id=\"");
//    str.append(id);
//    str.append("\"/>");
//    str.append("</DataTag>");
//    return str.toString();
//  }

  /**
   * @param minValue the minValue to set
   */
  public void setMinValue(Comparable minValue) {
    this.minValue = minValue;
  }


  /**
   * @param maxValue the maxValue to set
   */
  public void setMaxValue(Comparable maxValue) {
    this.maxValue = maxValue;
  }


  /**
   * @param address the address to set
   */
  public void setAddress(DataTagAddress address) {
    this.address = address;
  }

  /**
   * @return the daqTimestamp
   */
  @Override
  public Timestamp getDaqTimestamp() {
    return daqTimestamp;
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

  /**
   * @return the processId
   */
  @Override
  public Long getProcessId() {
    return processId;
  }

  /**
   * @param processId the processId to set
   */
  public void setProcessId(Long processId) {
    this.processId = processId;   
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
}
  