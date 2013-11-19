/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.client.configuration;

import java.util.Properties;

import cern.c2mon.shared.client.configuration.ConfigConstants.Action;
import cern.c2mon.shared.client.configuration.ConfigConstants.Entity;
import cern.c2mon.shared.client.configuration.ConfigConstants.Status;

/**
 * Configuration element stored in the TIMCONFIGELT and TIMCONFIGVAL database tables.
 * Also has an attached report.
 * 
 * <p>Notice that ConfigurationElements are only created for the 
 * configuration events specified in the database. Other resulting
 * changes are not instantiated as separate elements.
 * 
 * @author Mark Brightwell
 *
 */
public class ConfigurationElement {

  /**
   * The element id.
   */
  private Long sequenceId;
  
  /**
   * The configuration id.
   */
  private Long configId;
  
  /**
   * The type of Action of this element.
   */
  private Action action;
  
  /**
   * The type of element that is being reconfigured.
   */
  private Entity entity;
  
  /**
   * The id of the entity that needs configuring.
   */
  private Long entityId;
  
  /**
   * Status of the element, set once it has been executed.
   */
  private Status status = Status.OK;
  
  /**
   * Status on DAQ layer: can also be RESTART.
   */
  private Status daqStatus = Status.OK;
  
  /**
   * Stores the properties for CREATE and UPDATE elements
   * (stays null for REMOVE elements).
   */
  private Properties elementProperties;
  
  /**
   * @return the action
   */
  public Action getAction() {
    return action;
  }

  /**
   * Setter method.
   * @param action the action to set
   */
  public void setAction(final Action action) {
    this.action = action;
  }

  /**
   * Getter method.
   * @return the entity
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * Setter method.
   * @param entity the entity to set
   */
  public void setEntity(final Entity entity) {
    this.entity = entity;
  }

  /**
   * Getter method.
   * @return the sequenceId
   */
  public Long getSequenceId() {
    return sequenceId;
  }

  /**
   * Setter method.
   * @param sequenceId the sequenceId to set
   */
  public void setSequenceId(final Long sequenceId) {
    this.sequenceId = sequenceId;
  }

  /**
   * Getter method.
   * @return the confId
   */
  public Long getConfigId() {
    return configId;
  }

  /**
   * Setter method.
   * @param configId the confId to set
   */
  public void setConfigId(final Long configId) {
    this.configId = configId;
  }

  /**
   * Setter method
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Setter method.
   * @param status the status to set
   */
  public void setStatus(final Status status) {
    this.status = status;
  }

  /**
   * Getter method.
   * @return the entityId
   */
  public Long getEntityId() {
    return entityId;
  }

  /**
   * Setter method.
   * @param entityId the entityId to set
   */
  public void setEntityId(final Long entityId) {
    this.entityId = entityId;
  }

  /**
   * Getter method.
   * @return the elementProperties
   */
  public Properties getElementProperties() {
    return elementProperties;
  }

  /**
   * Setter method.
   * @param elementProperties the elementProperties to set
   */
  public void setElementProperties(final Properties elementProperties) {
    this.elementProperties = elementProperties;
  }

  /**
   * Setter method.
   * @param daqStatus
   */
  public void setDaqStatus(Status daqStatus) {
    this.daqStatus = daqStatus;
  }

  /**
   * Getter method.
   * @return
   */
  public Status getDaqStatus() {
    return daqStatus;
  }
  
}
