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
package cern.c2mon.server.lifecycle;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultLifecycleProcessor;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Service;

/**
 * This class is provided to solve the following
 * problem when running a distributed cache:
 * once the shutdown has started, the node is no
 * longer able to modify distributed objects, such
 * as acquiring a distributed lock. This prevents a
 * clean shutdown of the server.
 * 
 * To overcome this, the node is given a forewarning
 * and has 30 seconds max to finish processing any 
 * outstanding batch jobs.
 * 
 * Once the prepareForShutdown() method returns, a
 * response is send to the shutdown process (started
 * by the script) and the server process will be killed
 * (first gently, then forced).
 * 
 * @author Mark Brightwell
 *
 */
@Service
@ManagedResource(objectName="cern.c2mon:name=lifeCycleController")
public class LifeCycleController {

  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(LifeCycleController.class);
  
  /**
   * Spring's lifecycle management bean.
   */
  private DefaultLifecycleProcessor defaultLifecycleProcessor;
  
  private AtomicBoolean running = new AtomicBoolean(true);
  
  /**
   * Autowired constructor.
   * @param defaultLifecycleProcessor the lifecycle manager
   */
  @Autowired
  public LifeCycleController(final DefaultLifecycleProcessor defaultLifecycleProcessor) {
    super();
    this.defaultLifecycleProcessor = defaultLifecycleProcessor;
  }

  /**
   * Starts the process of shutting down this node. It is assumed
   * that this method should return within 30 seconds, at which
   * point the server should be ready to disconnect from the
   * distributed cache (in the case of a distributed setup).
   * 
   * <p>Only the first call to this method will have an effect. 
   * Subsequent calls are ignored.
   * 
   * Uses Spring's lifecycle management.
   */
  @ManagedOperation(description="Prepare server for shutdown")
  public void prepareForShutdown() {
    if (running.compareAndSet(true, false)) {
      LOGGER.info("Preparing server for shutdown");
      defaultLifecycleProcessor.stop();      
    }    
  }
  
}
