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
package cern.c2mon.daq.ssh.tools;

import java.util.Timer;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.address.SSHHardwareAddress;

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
