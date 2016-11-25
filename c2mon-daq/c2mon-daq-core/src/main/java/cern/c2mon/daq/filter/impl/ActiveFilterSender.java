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
package cern.c2mon.daq.filter.impl;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import cern.c2mon.daq.config.DaqProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;

import cern.c2mon.daq.config.ProcessMessageSenderConfig;
import cern.c2mon.daq.filter.FilterMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.shared.daq.filter.FilteredDataTagValueUpdate;

/**
 * ActiveMQ implementation of the FilterMessageSender, sending filtered updates
 * to the statistics consumer application.
 * <p/>
 * This class gets instantiated by {@link ProcessMessageSenderConfig}
 *
 * @author Mark Brightwell
 *
 */
@Slf4j
public class ActiveFilterSender extends FilterMessageSender implements IFilterMessageSender {

  /**
   * The Spring template for sending the updates (creates a session for each call!).
   */
  private JmsTemplate filterTemplate;

  public ActiveFilterSender(final JmsTemplate filterTemplate, DaqProperties properties) {
    super(properties);
    this.filterTemplate = filterTemplate;
  }

  @Override
  public void connect() {
    // nothing to do, connects automatically (in separate DriverKernel thread)
  }

  @Override
  public void shutdown() {
    super.closeTagBuffer();
  }

  @Override
  protected void processValues(final FilteredDataTagValueUpdate filteredDataTagValueUpdate) throws JMSException {
    log.trace("entering FilterMessageSender processValues()...");

    // prepare the message
    filterTemplate.send(session -> {
      TextMessage msg = session.createTextMessage();
      msg.setText(filteredDataTagValueUpdate.toXML());
      return msg;
    });

    // log the filtered values in the log file
    filteredDataTagValueUpdate.log();

    log.trace("leaving FilterMessageSender processValues()");
  }

}
