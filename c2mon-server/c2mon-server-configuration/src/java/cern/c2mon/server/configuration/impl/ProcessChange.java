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
package cern.c2mon.server.configuration.impl;

import cern.c2mon.shared.daq.config.IChange;

/**
 * Change event to be sent to a Process. Contains the event itself that
 * needs sending and a reference to the Process the change needs sending
 * to.
 * 
 * <p>Used within server only, to transfer Change events to the correct DAQ.
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessChange {

  /**
   * The id of the Process this Change event is destined for.
   */
  private Long processId;
  
  /**
   * The Change event itself (the object sent to the DAQ layer via JMS).
   */
  private IChange changeEvent;
  
  /**
   * Flag indicating that this change requires a process reboot.   
   */
  private boolean requiresReboot = false;

  
  /**
   * When no action should be taken on the DAQ layer use this
   * constructor (i.e. not reconfiguration, no reboot).
   */
  public ProcessChange() {
    this.requiresReboot = false;    
  }
  
  /**
   * Use this constructor if no change needs sending to
   * the DAQ layer. Sets reboot flag to true.
   * @param processId id of affected process
   */
  public ProcessChange(final Long processId){
    this.processId = processId;
    this.requiresReboot = true;
  }
  
  /**
   * Constructor.
   * @param processId the id of the Process this change should be sent to
   * @param changeEvent the Change event itself
   */
  public ProcessChange(final Long processId, final IChange changeEvent) {
    super();
    this.processId = processId;
    this.changeEvent = changeEvent;
  }

  /**
   * Getter method.
   * @return the changeEvent
   */
  public IChange getChangeEvent() {
    return changeEvent;
  }

  /**
   * Getter method.
   * @return the process
   */
  public Long getProcessId() {
    return processId;
  }
  
  /**
   * Returns true if the change needs sending to the process.
   * If false, the processId field points to the affected
   * process: a restart is required.
   * @return true if DAQ action required
   */
  public boolean processActionRequired() {
    return changeEvent != null;
  }
  
  /**
   * Indicates if the Process needs rebooting for the changes
   * to apply. Only available for changes that do not need propagating
   * to the DAQ (for these changes, the DAQ makes the reboot decision).
   * @return true if Process reboot required
   */
  public boolean requiresReboot() {
    if (processActionRequired()) {
      throw new UnsupportedOperationException("The method is not available for configuration changes that need sending to the DAQ!");
    } else {
      return requiresReboot;
    }    
  }
  
  /**
   * Set the reboot flag for this Process change.
   * @param reboot true if the DAQ should be restarted
   */
  public void requiresReboot(boolean reboot){
    requiresReboot = reboot;
  }
    
  
}
