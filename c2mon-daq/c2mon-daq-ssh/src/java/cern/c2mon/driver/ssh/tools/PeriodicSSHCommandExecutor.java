/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
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
package cern.c2mon.driver.ssh.tools;

import java.util.Timer;

import cern.tim.shared.common.datatag.address.SSHHardwareAddress;
import cern.tim.shared.daq.datatag.ISourceDataTag;

/**
 * This class implements periodic ssh task execution mechanisms. It is used
 * by the data tags, that needs to be periodically updated, and which values
 * rely on ssh commands execution states
 * 
 * @author vilches
 */
public class PeriodicSSHCommandExecutor {

  /**
   * The standard java timer
   */
  private Timer timer;
  ISourceDataTag tag;

  /**
   * SSH Helper class with some helping methods.
   */
  private SSHHelper sshHelper;

  /**
   * The constructor.
   * 
   * @param owner
   * @roseuid 43302FE800DB
   */
  public PeriodicSSHCommandExecutor(ISourceDataTag dtag, SSHHelper sshHelper) {
    this.tag = dtag;
    this.sshHelper = sshHelper;
    timer = new Timer();

    long interval = ((SSHHardwareAddress) tag.getHardwareAddress()).getCallInterval() * 1000; // in
    // ms
    long delay = ((SSHHardwareAddress) tag.getHardwareAddress()).getCallDelay() * 1000; // in
    // ms
    // normalize the values            
    if (delay < 0)
      delay = 0;

    this.setInterval(interval, delay);
  }

  /**
   * This method sets the timer's 'tick' interval
   * 
   * @param milisecondsInterval
   * @roseuid 43302FE803D4
   */
  protected void setInterval(long milisecondsInterval, long delay) {
    timer.schedule(new PeriodicSSHTask(this.tag, this.sshHelper), delay, milisecondsInterval);
  }

  /**
   * This method is used for timer's termination
   * 
   * @roseuid 43302FEA0283
   */
  public void terminateTimer() {
    // Terminate the timer thread
    timer.cancel();
  }
}
