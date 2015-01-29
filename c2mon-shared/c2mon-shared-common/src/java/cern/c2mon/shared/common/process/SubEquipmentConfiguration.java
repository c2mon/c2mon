/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
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
package cern.c2mon.shared.common.process;

/**
 * This class is responsible for keeping SubEquipment configuration parameters.
 *
 * @author Justin Lewis Salmon
 */
public class SubEquipmentConfiguration {

  /**
   * Unique SubEquipment identifier.
   */
  private Long id;

  /**
   * Identifier of the data tag that is used to signal a communication fault to
   * the server.
   */
  private Long commFaultTagId;

  /**
   * Value that the communication fault tag has to take if there is a
   * communication problem with the SubEquipment.
   */
  private boolean commFaultTagValue;

  /**
   * Identifier of the SubEquipment alive tag.
   */
  private Long aliveTagId;

  /**
   * Interval (ms) between 2 SubEquipment alive tags.
   */
  private Long aliveInterval;

  /**
   * SubEquipment name.
   */
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
   * @return the id
   */
  public Long getId() {
    return id;
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
