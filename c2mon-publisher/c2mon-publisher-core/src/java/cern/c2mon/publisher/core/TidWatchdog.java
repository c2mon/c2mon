/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2012 CERN. This program is free software; you can
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
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.publisher.core;

import java.io.File;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.apache.log4j.helpers.FileWatchdog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A File watchdog process which checks every 60 seconds if the file with
 * provides the list of tag ids has changed.
 * 
 * @author Matthias Braeger
 */
@Service
class TidWatchdog extends FileWatchdog {
  
  /** Environment variable that defines the location of the tid file as URL */
  private static final String PUBLISHER_TID_FILE = 
    System.getProperty("c2mon.publisher.tid.location", "conf/publisher.tid");
  
  /** The Log4j's logger */
  private static Logger logger = Logger.getLogger(FileWatchdog.class);
  
  /** The publisher core gateway service */
  private final Gateway gateway;
  
  /**
   * Default Constructor
   * 
   * Initializes the TID watchdag to scan for the default file "conf/publisher.tid".
   * Alternatively, the default location can be overwritten with the following Java
   * environment variable: 
   * <p>
   * <code>-Dpublisher.tid.location</code>
   * @param gateway The publisher core gateway
   */
  @Autowired
  public TidWatchdog(final Gateway gateway) {
    super(PUBLISHER_TID_FILE);
    this.gateway = gateway;
  }
  
  /**
   * Starts the watchdog thread
   */
  @PostConstruct
  public void startWatchdog() {
    checkTidFileExists();
    logger.debug("Starting TID watchdog for subscribing to all data tags which IDs are listed in file " + PUBLISHER_TID_FILE);
    doOnChange();
    super.start();
  }
  
  /**
   * Tests if the TID file exists, otherwise it throws a runtime exception.
   * @throws RuntimeException In case the TID file does not exists
   */
  private void checkTidFileExists() throws RuntimeException {
    File file = new File(PUBLISHER_TID_FILE);
    if (!file.exists()) {
      throw new RuntimeException("Cannot find TID file: " + PUBLISHER_TID_FILE);
    }
  }

  /**
   * Gets called every time a change of the TID file is detected
   */
  @Override
  protected void doOnChange() {
    logger.info("TID file has changed!");
    
    if (gateway != null && !this.gateway.subscribeDataTags(new File(PUBLISHER_TID_FILE))) {
      logger.error("Unable to successfully parse data tag file " + PUBLISHER_TID_FILE);
    }
  }
}
