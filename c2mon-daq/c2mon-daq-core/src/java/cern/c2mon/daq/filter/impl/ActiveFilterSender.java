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

import cern.c2mon.daq.config.Options;
import cern.c2mon.daq.common.conf.core.ConfigurationController;
import cern.c2mon.daq.filter.FilterMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.shared.daq.filter.FilteredDataTagValueUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * ActiveMQ implementation of the FilterMessageSender, sending filtered updates
 * to the statistics consumer application.
 *
 * @author Mark Brightwell
 *
 */
@Component//("filterMessageSender")
@Profile({ "single", "double" })
public class ActiveFilterSender extends FilterMessageSender implements IFilterMessageSender {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ActiveFilterSender.class);

  /**
   * The Spring template for sending the updates (creates a session for each call!).
   */
  private JmsTemplate filterTemplate;

  @Autowired
  private Environment environment;

  /**
   * Constructor called by Spring.
   * @param configurationController to access the configuration (run options)
   * @param filterTemplate the Spring template to send the message (defined in XML)
   */
  @Autowired
  public ActiveFilterSender(final ConfigurationController configurationController,
                            @Qualifier("filterJmsTemplate") final JmsTemplate filterTemplate) {
    super(configurationController);
    this.filterTemplate = filterTemplate;
  }

  @Override
  public void connect() {
    //nothing to do, connects automatically (in separate DriverKernel thread)
  }

  @Override
  public void shutdown() {
    super.closeTagBuffer();
  }

  @Override
  protected void processValues(final FilteredDataTagValueUpdate filteredDataTagValueUpdate) throws JMSException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("entering FilterMessageSender processValues()...");
    }


    // skip this part if in test mode or filtering is not enabled,
    if (!environment.containsProperty(Options.FILTER_ENABLED) && environment.getProperty(Options.FILTER_ENABLED, Boolean.class)) {
        // prepare the message
        filterTemplate.send(new MessageCreator() {

          @Override
          public Message createMessage(final Session session) throws JMSException {
            TextMessage msg = session.createTextMessage();
            msg.setText(filteredDataTagValueUpdate.toXML());
            return msg;
          }
        });

    }

    // log the filtered values in the log file
    filteredDataTagValueUpdate.log();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("leaving FilterMessageSender processValues()");
    }
  }

}
