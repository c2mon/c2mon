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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import cern.c2mon.daq.common.messaging.ProcessMessageReceiver;
import cern.c2mon.shared.daq.messaging.DAQResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dummy implementation of the ProcessMessageReceiver that can be used in the
 * Spring XML to start up without the DAQ command/request functionality.
 *
 * @author mbrightw
 */
@Component("activeMessageReceiver")
@Profile("test")
public class DummyMessageReceiver extends ProcessMessageReceiver {

  @Override
  public void connect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void sendDAQResponse(DAQResponse response, Destination destination, Session session) throws JMSException {

  }

  @Override
  public void disconnect() {
    // TODO Auto-generated method stub

  }


  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

}
