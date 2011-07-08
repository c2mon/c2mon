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
package cern.c2mon.client.jms;

/**
 * Specifies the details needed by the JmsProxy to register a 
 * {@link ServerUpdateListener} to incoming {@link TransferTagValue}s.
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
  Long getId();
  
  /**
   * The topic name on which these updates are expected.
   * 
   * @return the JMS topic name as String
   */
  String getTopicName();
  
}
