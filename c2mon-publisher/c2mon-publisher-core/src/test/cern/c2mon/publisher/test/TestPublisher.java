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
package cern.c2mon.publisher.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;

@Service
public class TestPublisher implements Publisher {

  /** LOG4J logger instance */
  private static final Logger LOG = LoggerFactory.getLogger(TestPublisher.class);
  
  /** tag update counter */
  private static int counter = 0;
  
  @Override
  public void onUpdate(final Tag cdt, final TagConfig cdtConfig) {
    LOG.debug("Got update for tag " + cdt.getId());
    counter++;
  }

  @Override
  public void shutdown() {
    // Do nothing
  }
  
  /**
   * @return The number of update calls received
   */
  public static int getUpdateCounter() {
    return counter;
  }

}
