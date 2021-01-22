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
package cern.c2mon.client.core.jms;

/**
 * Specifies the details needed by the JmsProxy to register a 
 * {@link ServerUpdateListener} to incoming {@link TagValueUpdate}s.
 * 
 * <p>These details are used by the JmsProxy to register on JMS for
 * the updates and direct them to the appropriate listener.
 * 
 * <p>For C2MON, these details are to be found in the ClientDataTag.
 * 
 * @author Mark Brightwell
 *
 */
public interface TopicRegistrationDetails {

  /**
   * Id of the TransferTagValues to register to (corresponds to
   * ClientDataTag id in C2MON).
   * 
   * @return the id of the TransferTagValues of interest
   */
  long getId();
  
  /**
   * The topic name on which these updates are expected.
   * 
   * @return the JMS topic name as String
   */
  String getTopicName();
  
}
