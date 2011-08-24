/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.history.gui.components;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.PlaybackControl;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.core.C2monServiceGateway;

/**
 * This label is the gui representation of a clock instance. The format can be
 * changed through the constant <code>clockFormatter</code>.
 * 
 * @author Michael Berberich
 */
public class ClockLabel extends JLabel {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = 6963677989402955598L;

  /** The date and time formatter */
  private final DateFormat clockFormatter = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");

  /**
   * Constructor
   */
  public ClockLabel() {
    this.updateTime();
  }

  /**
   * Updates the label according to the clock's current time.
   */
  public void updateTime() {
    HistoryPlayer historyPlayer;
    try {
      historyPlayer = C2monServiceGateway.getHistoryManager().getHistoryPlayer();
    }
    catch (HistoryPlayerNotActiveException e) {
      historyPlayer = null;
    }
    
    if (historyPlayer == null) {
      this.setText("Uninitialized...");
    }
    else {
      final Long time = historyPlayer.getPlaybackControl().getClockTime();
      if (time < historyPlayer.getStart().getTime()) {
        this.setText("Initializing...");
      }
      else {
        this.setText(clockFormatter.format(new Date(time)));
      }
    }
    
  }
}
