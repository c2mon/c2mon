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
package cern.c2mon.daq.common.messaging.impl;

import cern.c2mon.daq.common.messaging.JmsSender;
import cern.c2mon.shared.common.datatag.DataTagValueUpdate;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;

import javax.jms.JMSException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for testing purposes and for the TestMode
 */
public class DummyJmsSender implements JmsSender {


  private List<SourceDataTagValue> messages = new ArrayList<SourceDataTagValue>();

  /**
   * Enabling/disabling the action of sending information to the brokers
   */
  private boolean isEnabled = true;

  @Override
  public void connect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void processValue(SourceDataTagValue sourceDataTagValue) {
    messages.add(sourceDataTagValue);

  }

  @Override
  public void processValues(DataTagValueUpdate dataTagValueUpdate) {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  public List<SourceDataTagValue> getMessages() {
    return this.messages;
  }

  /**
   * Sets the isEnabled current value
   *
   * @param value Enabling/disabling the action of sending information to the brokers
   */
  @Override
  public final void setEnabled(final boolean value) {
    this.isEnabled = value;
  }

  /**
   * Gets the isEnabled current value
   *
   * @return isEnabled Current status of the action of sending information to the brokers
   */
  @Override
  public final boolean getEnabled() {
    return this.isEnabled;
  }
}
