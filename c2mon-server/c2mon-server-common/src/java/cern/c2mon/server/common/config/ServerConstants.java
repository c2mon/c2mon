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
package cern.c2mon.server.common.config;

/**
 * Contains constants used across the server modules.
 * @author Mark Brightwell
 *
 */
public final class ServerConstants {

  /**
   * Is not supposed to be instantiated.
   */
  private ServerConstants() { };
  
  /**
   * Determines a bean lifecycle start-up/shutdown order.
   * Beans managing the lifecycle of a module should implement
   * the Spring SmartLifeCycle interface. The getPhase() method
   * should then return one of these constants, according to when
   * this bean should be stopped during the server shutdown. 
   * 
   * <p>Beans managing the lifecycle of components listening to 
   * data/request arriving from outside the server will be started last 
   * and stopped first and should be in the START_LAST phase 
   * (e.g. JMS listeners). 
   * 
   * <p>Beans processing requests received from within the server 
   * should be put in the PHASE_INTERMEDIATE phase (e.g. cache listeners).
   * [in the server core, the layer between listeners and cache - buffering
   * the updates for instance - stops at phase PHASE_INTERMEDIATE + 1, so
   * has time to empty buffers before the listeners are shutdown]
   * 
   * <p>Services that will be needed at shutdown until the last moment
   * should be put in the PHAST_STOP_LAST phase (for example, services used
   * by DB logging facilities called when shutting down).
   */
  public static final int PHASE_START_LAST = 10;
  public static final int PHASE_INTERMEDIATE = 0;
  public static final int PHASE_STOP_LAST = -10;
  
  
}
