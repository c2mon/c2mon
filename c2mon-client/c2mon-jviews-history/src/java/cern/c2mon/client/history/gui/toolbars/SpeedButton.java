/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.history.gui.toolbars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.event.PlaybackControlListener;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.core.C2monServiceGateway;

/**
 * The speed button for changing the speed of the history playback
 * 
 * @author vdeila
 */
public class SpeedButton extends StandardButton implements PlaybackControlListener {

  /** serialVersionUID */
  private static final long serialVersionUID = 5687175488785601842L;

  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(SpeedButton.class);

  /** When pressing the speed button it uses these intervals */
  private static final double[] CLOCK_SPEEDS = new double[] {
    1.0, 5.0, 10.0, 20.0, 60.0, 5.0 * 60.0, 10.0 * 60.0, 30.0 * 60.0, 60.0 * 60.0, 3.0 * 60.0 * 60.0
  };
  
  /** The popup menu showing the different speeds */
  private JPopupMenu speedPopupMenu;
  
  /**
   * Creates a speed button
   */
  public SpeedButton() {
    super();
    
    this.setHideActionText(false);
    this.setPreferredSize(null);
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        switch (e.getButton()) {
        case MouseEvent.BUTTON1: // Left mouse button
          // Shows the speed popup menu
          speedPopupMenu.setLocation(getLocationOnScreen());
          speedPopupMenu.setVisible(true);
          break;
        case MouseEvent.BUTTON2: // Middle mouse button
          // Sets the clock speed to 1x
          setClockSpeed(1);
          break;
        case MouseEvent.BUTTON3: // Right mouse button
          break;
        default:
          break;
        }
      }
    });
    updateText();
    
    this.speedPopupMenu = new JPopupMenu();
    for (final double speed : CLOCK_SPEEDS) {
      final JMenuItem menuItem = new JMenuItem(getSpeedString(speed));
      menuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          setClockSpeed(speed);
        }
      });
      this.speedPopupMenu.add(menuItem);
    }
    speedPopupMenu.setInvoker(this);
  }
  

  /** Updates the label of the speed button */
  public void updateText() {
    this.setText(getClockSpeedString());
  }
  
  /**
   * 
   * @param newSpeed The new speed to set
   */
  private void setClockSpeed(final double newSpeed) {
    try {
      C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().setPlaybackSpeed(newSpeed);
    }
    catch (HistoryPlayerNotActiveException e) {
      LOG.debug("Cannot change the clock speed", e);
    }
  }
  
  /**
   * 
   * @return A string representation of the current clock speed
   */
  private String getClockSpeedString() {
    try {
      return getSpeedString(C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().getPlaybackSpeed());
    }
    catch (HistoryPlayerNotActiveException e) {
      return "Unknown";
    }
  }
  
  /**
   * @param speed The speed to get a string representation of
   * @return A string representation of the speed
   */
  private String getSpeedString(final double speed) {
    double speedConverted = speed;
    String unit = "s";
    if (speedConverted >= 60 * 2) {
      speedConverted = speedConverted / 60.0;
      unit = "m";
      if (speedConverted >= 60) {
        speedConverted = speedConverted / 60.0;
        unit = "h";
      }
    }
    return String.format("%.0f%s/s", speedConverted, unit);
  }


  @Override
  public void onClockPlaybackSpeedChanged(final double newMultiplier) {
    setText(getSpeedString(newMultiplier));
  }

  @Override
  public void onClockTimeChanged(final long newTime) { }
  @Override
  public void onClockTimeChanging(final long newTime) { }
  @Override
  public void onPlaybackStarting() { }
  @Override
  public void onPlaybackStopped() { }
}
