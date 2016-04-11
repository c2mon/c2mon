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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.daq.common.messaging.JmsLifecycle;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.shared.common.filter.FilteredDataTagValue;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Dummy implementation of the FilterMessageSender which can be plugged into the
 * application when no filtering is needed . (notice that filtering can also be
 * disabled using a command line parameter, but this is not necessary supported
 * by all implementations yet, so TODO : connect is not called in this case, but
 * disconnect and addValue still are, and this should not be the case so as to
 * be implementation independent...).
 *
 * @author mbrightw
 */
@Component("filterMessageSender")
@Profile("test")
public class DummyFilterSender implements IFilterMessageSender, JmsLifecycle {

  /*
   * The system logger.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger("FilteredDataTagLogger");

  @Override
  public void connect() {
    // TODO Auto-generated method stub

  }

  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }

  @Override
  public void addValue(FilteredDataTagValue dataTagValue) {
    LOGGER.info(dataTagValue.toString());
  }

}
