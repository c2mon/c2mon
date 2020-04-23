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
package cern.c2mon.daq.common.messaging;

import org.springframework.jms.JmsException;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

/**
 * The interface that must be implemented by a class used to send updates
 * to the server via JMS.
 * @author mbrightw
 *
 */
public interface JmsSender {

  /**
   * Connects to the JMS broker.
   */
  void connect();

  /**
   * Disconnects from the JMS broker. In DAQ core, only used on shutdown of the DAQ.
   */
  void disconnect();

  /**
   * The processValues method creates an XML JMS message with a content of the
   * set SourceDataTagsValue objects (encapsulated inside DataTagValueUpdate object).
   * This method is called automatically every time a PullEvent comes from dataTags
   * synchrobuffer.
   * @param dataTagValueUpdate the collection of SourceDataTagValue's to send
   * @throws JmsException if a JMS exception is caught while sending the values
   */
  void processValues(DataTagValueUpdate dataTagValueUpdate);

  /**
   * The ProcessValue method creates a JMS message with a content of the DataTagValue
   * object (passed as an argument) encoded into XML.
   * @param sourceDataTagValue the source value to send
   * @throws JmsException if a JMS exception occurs
   */
  void processValue(SourceDataTagValue sourceDataTagValue);

  /**
   * Do final shutdown.
   */
  void shutdown();

  /**
   * Sets the isEnabled current value
   *
   * @param value Enabling/disabling the action of sending information to the brokers
   */
  void setEnabled(final boolean value);

  /**
   * Gets the isEnabled current value
   *
   * @return isEnabled Current status of the action of sending information to the brokers
   */
  boolean getEnabled();
}
