/*******************************************************************************
 * This file is part of the CERN Control and Monitoring Platform (C2MON)
 * project. See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
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
 * Author: C2MON team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.shared.client.tag;

import java.util.Collection;


/**
 * This interface defines the transport object that is transfered to the client
 * layer for initializing a given tag. Furthermore it is used to communicate
 * configuration changes.
 * 
 * @author Matthias Braeger
 * @see TagValueUpdate
 */
public interface TagUpdate extends TagValueUpdate {
  
  /**
   * Returns all process id's which are relevant to compute the
   * final quality status on the C2MON client layer. By defintition there
   * is just one id defined. Only rules might have dependencies
   * to multiple processes (DAQs).
   * 
   * @return The list of process id dependencies
   */
  Collection<Long> getProcessIds();
  
  
  /** 
   * Returns all equipment id's which are relevant to compute the
   * final quality status on the C2MON client layer. By definition there
   * is just one id defined. Only rules might have dependencies
   * to multiple equipments.
   * 
   * @return The list of equipment id dependencies
   */
  Collection<Long> getEquipmentIds();
  
  
  /**
   * Returns the unit of the value
   * @return The unit of the value
   */
  String getUnit();


  /**
   * @return The String representation of the <code>RuleExpression</code> object
   * or null, if the tag does not represent a rule.
   */
  String getRuleExpression();


  /**
   * Returns the unique tag name
   * @return the unique tag name
   */
  String getName();


  /**
   * @return A <code>String</code> representation of the JMS destination where the DataTag 
   *         is published on change.
   */
  String getTopicName();
}
