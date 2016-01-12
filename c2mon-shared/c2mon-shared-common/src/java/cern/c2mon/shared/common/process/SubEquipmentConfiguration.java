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
package cern.c2mon.shared.common.process;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * This class is responsible for keeping SubEquipment configuration parameters.
 *
 * @author Justin Lewis Salmon
 */
public class SubEquipmentConfiguration {

  /**
   * Unique SubEquipment identifier.
   */
  @Attribute
  private Long id;

  /**
   * Identifier of the data tag that is used to signal a communication fault to
   * the server.
   */
  @Element(name = "commfault-tag-id")
  private Long commFaultTagId;

  /**
   * Value that the communication fault tag has to take if there is a
   * communication problem with the SubEquipment.
   */
  @Element(name = "commfault-tag-value")
  private boolean commFaultTagValue;

  /**
   * Identifier of the SubEquipment alive tag.
   */
  @Element(name = "alive-tag-id")
  private Long aliveTagId;

  /**
   * Interval (ms) between 2 SubEquipment alive tags.
   */
  @Element(name = "alive-interval")
  private Long aliveInterval;

  /**
   * SubEquipment name.
   */
  @Attribute
  private String name;

  /**
   * TODO
   *
   * @param id
   * @param name
   * @param commFaultTagId
   * @param commFaultTagValue
   */
  public SubEquipmentConfiguration(Long id, String name, Long commFaultTagId, boolean commFaultTagValue) {
    this.id = id;
    this.name = name;
    this.commFaultTagId = commFaultTagId;
    this.commFaultTagValue = commFaultTagValue;
  }

  /**
   * No-arg constructor (needed for XML deserialisation).
   */
  public SubEquipmentConfiguration() {
  }

  /**
   * @return the id
   */
  public Long getId() {
    return id;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the commFaultTagId
   */
  public Long getCommFaultTagId() {
    return commFaultTagId;
  }

  /**
   * @return the commFaultTagValue
   */
  public boolean getCommFaultTagValue() {
    return commFaultTagValue;
  }

  /**
   * @return the aliveTagId
   */
  public Long getAliveTagId() {
    return aliveTagId;
  }

  /**
   * @param aliveTagId the aliveTagId to set
   */
  public void setAliveTagId(Long aliveTagId) {
    this.aliveTagId = aliveTagId;
  }

  /**
   * @return the aliveInterval
   */
  public Long getAliveInterval() {
    return aliveInterval;
  }

  /**
   * @param aliveInterval the aliveInterval to set
   */
  public void setAliveInterval(Long aliveInterval) {
    this.aliveInterval = aliveInterval;
  }
}
