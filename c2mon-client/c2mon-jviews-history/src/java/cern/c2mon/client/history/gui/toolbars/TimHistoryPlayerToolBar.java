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

package cern.c2mon.client.history.gui.toolbars;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.ext.history.C2monHistoryGateway;
import cern.c2mon.client.ext.history.C2monHistoryManager;
import cern.c2mon.client.ext.history.common.HistoryPlayer;
import cern.c2mon.client.ext.history.common.event.HistoryPlayerAdapter;
import cern.c2mon.client.ext.history.common.event.PlaybackControlAdapter;
import cern.c2mon.client.ext.history.common.event.PlaybackControlListener;
import cern.c2mon.client.ext.history.common.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.history.gui.components.TimeSlider;
import cern.c2mon.client.history.gui.components.event.BufferedChangeListener;

/**
 * The <code>TimHistoryPlayerToolBar</code> is used to provide the user with
 * controls for the history player.
 * 
 * @author Michael Berberich
 */
public final class TimHistoryPlayerToolBar extends JToolBar {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = 3434359226623834230L;
  
  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(TimHistoryPlayerToolBar.class);
  
  /**
   * When moving one tick on the time slider, it moves this amount in time
   * (milliseconds)
   */
  private static final long SMALLEST_AMOUNT_OF_TIME = 1000 * 15;
  
  /** The minumum value to be set for the maximum value. */
  private static final long MINUMUM_VALUE_FOR_THE_MAXIMUM = 80;
  
  /** The timer's update rate */
  private static final int UPDATE_RATE = 90;
  
  /** The date and time formatter */
  public static final DateFormat CLOCK_FORMATTER = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
  
  /** The clock button */
  private AbstractButton clockButton;
  
  /** A time slider that indicates the current position of the history player */
  private TimeSlider timeSlider;

  /** The play/pause button */
  private final ResumeButton playButton;
  
  /** The speed button */
  private final SpeedButton speedButton;
  
  /** A swing timer to update swing components that depend on the clock */
  private Timer timer;
  
  /**
   * The playback control listener is listening to when the player resumes or
   * pauses
   */
  private PlaybackControlListener playbackControlEvents = null;

  /** The history manager */
  private final C2monHistoryManager historyManager;
  
  /** Popup for extending the history playback time */
  private ExtendHistoryTimePopup extendHistoryTimePopup = null;
  
  /**
   * Constructor
   */
  public TimHistoryPlayerToolBar() {
    super(HORIZONTAL);
    this.setBorder(BorderFactory.createEmptyBorder());
    this.setOpaque(false);
    
    this.playButton = new ResumeButton();
    this.speedButton = new SpeedButton();
    
    this.clockButton = ToolbarButtonFactory.createButton();
    this.clockButton.setHideActionText(false);
    this.clockButton.setPreferredSize(null);
    this.clockButton.setBorderPainted(false);
    this.clockButton.setFocusPainted(false);
    this.clockButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          // Left mouse button was clicked
          try {
            historyManager.getHistoryPlayer().getPlaybackControl().pause();
          }
          catch (HistoryPlayerNotActiveException e1) {
            LOG.warn("Cannot pause the history playback, because the history player could not be retrieved", e1);
          }
          if (extendHistoryTimePopup == null) {
            extendHistoryTimePopup = new ExtendHistoryTimePopup();
          }
          extendHistoryTimePopup.show(TimHistoryPlayerToolBar.this, clockButton.getLocationOnScreen());
        }
      }
      @Override
      public void mouseEntered(MouseEvent e) {
        clockButton.setBorderPainted(true);
      }
      @Override
      public void mouseExited(MouseEvent e) {
        clockButton.setBorderPainted(false);
      }
    });

    this.timeSlider = new TimeSlider();
    
    this.add(this.playButton);
    this.add(this.speedButton);
    this.add(this.timeSlider);
    this.add(this.clockButton);

    this.setFloatable(false);
    
    historyManager = C2monHistoryGateway.getHistoryManager();
    
    // Install listener
    historyManager.getHistoryPlayerEvents().addHistoryPlayerListener(new HistoryPlayerEvents());
    
    installBufferedChangeEventListener();
    installTimeSliderListener();
    installUpdateGuiTimer();
  }
  
  /**
   * Installs a timer which updates the GUI elements The frequency this method
   * is called can be modified through the constant <code>UPDATE_RATE</code>.
   */
  private void installUpdateGuiTimer() {
    this.timer = new Timer(UPDATE_RATE, new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        updateGuiElements();
      }
    });
  }
  
  /**
   * Installs change listener on the timeslider
   */
  private void installBufferedChangeEventListener() {
    this.timeSlider.addChangeListener(new BufferedChangeListener() {
      @Override
      public void stateChanged(final ChangeEvent e) {
      }
      
      @Override
      public void valueForced(final ChangeEvent e) {
        // Is called when someone tries to change the value to more than what is loaded.
        
        try {
//          if (C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().isPlaying()) {
//            // If the clock is running it is paused
//            //pause();
//          }
          C2monHistoryGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().setClockTime(computeTimeFromTimeSlider());
        }
        catch (final HistoryPlayerNotActiveException e1) {
          if (LOG.isDebugEnabled()) {
            LOG.debug("Cannot set the clock time", e1);
          }
        }
      }
    });
  }
  
  /**
   * Events for when the history player activated, deactivated, initializing,
   * etc.
   */
  class HistoryPlayerEvents extends HistoryPlayerAdapter {

    @Override
    public void onActivatedHistoryPlayer() {
      try {
        final HistoryPlayer historyPlayer = C2monHistoryGateway.getHistoryManager().getHistoryPlayer();
        if (playbackControlEvents == null) {
          playbackControlEvents = new PlaybackControlEvents();
        }
        historyPlayer.getPlaybackControl().addPlaybackControlListener(playbackControlEvents);
        historyPlayer.getPlaybackControl().addPlaybackControlListener(playButton);
        historyPlayer.getPlaybackControl().addPlaybackControlListener(speedButton);
        
        updateGuiElements();
        speedButton.updateText();
      }
      catch (HistoryPlayerNotActiveException e) {
        LOG.warn("The history player is not available, in the activating history player event..", e);
      }
    }

    @Override
    public void onDeactivatingHistoryPlayer() {
      // Removes the listener from playbackControl
      if (playbackControlEvents != null) {
        try {
          final HistoryPlayer historyPlayer = historyManager.getHistoryPlayer();
          historyPlayer.getPlaybackControl().removePlaybackControlListener(playbackControlEvents);
          historyPlayer.getPlaybackControl().removePlaybackControlListener(playButton);
          historyPlayer.getPlaybackControl().removePlaybackControlListener(speedButton);
        }
        catch (HistoryPlayerNotActiveException e) {
          LOG.warn("Cannot remove listener from HistoryPlayer, because it is not available.", e);
        }
      }
    }

    @Override
    public void onInitializingHistoryStarted() {
      timeSlider.setPercentLoaded(0);
      updateGuiElements();
    }

    @Override
    public void onInitializingHistoryFinished() {
      
    }

    @Override
    public void onHistoryDataAvailabilityChanged(final Timestamp newTime) {
      double percentLoaded = 0;
      try {
        long historyStartTime = historyManager.getHistoryPlayer().getStart().getTime();
        final long historyEndTime = historyManager.getHistoryPlayer().getEnd().getTime();

        final long totalTime = historyEndTime - historyStartTime;
        final long loadedTime = newTime.getTime() - historyStartTime;
        percentLoaded = loadedTime / (double) totalTime;
      }
      catch (HistoryPlayerNotActiveException e) {
        percentLoaded = 0;
      }
      timeSlider.setPercentLoaded(percentLoaded);
    }

    @Override
    public void onHistoryIsFullyLoaded() {
      timeSlider.setPercentLoaded(1.0);
    }
  }
  
  /**
   * Events for when the history player is paused, resumed, etc.
   */
  private class PlaybackControlEvents extends PlaybackControlAdapter {

    @Override
    public void onClockTimeChanged(long newTime) {
      // updateClockLabel();
      updateGuiElements();
    }

    @Override
    public void onPlaybackStarting() {
      setTimeSliderEnabled(false);

      timer.start();
    }

    @Override
    public void onPlaybackStopped() {
      setTimeSliderEnabled(true);
      updateGuiElements();

      timer.stop();
    }
  }
  
  /**
   * Installs a mouse listener on the time slider 
   */
  private void installTimeSliderListener() {
    this.timeSlider.addMouseListener(new MouseAdapter() {

      @Override
      public void mouseReleased(final MouseEvent e) {
        if (timeSlider.isEnabled()) {
          setTimeSliderEnabled(false);
          playButton.setEnabled(false);
          final Thread changingTimeThread = new Thread(new Runnable() {
            @Override
            public void run() {
              try {
                final long newTime = computeTimeFromTimeSlider();
                historyManager.getHistoryPlayer().getPlaybackControl().setClockTime(newTime);
                
                if (LOG.isDebugEnabled()) {
                  LOG.debug("Changed the time to " + new Timestamp(newTime).toString());
                }
              }
              catch (HistoryPlayerNotActiveException e) {
                LOG.error("Couldn't change the time, because the history player mode is not active..", e);
              }
              finally {
                setTimeSliderEnabled(true);
                playButton.setEnabled(true);
              }
            }
          });
          changingTimeThread.setName("New-Time-Thread");
          changingTimeThread.start();
        }
      }
    });
  }

  /**
   * @param enable
   *          <code>true</code> to show the timeSlider
   */
  private void setTimeSliderEnabled(final boolean enable) {
    timeSlider.setEnabled(enable);
  }
  
  /**
   * Sets the value of the slider. This method is normally called by the swing
   * timer of the <code>TimHistoryPlayerToolbar</code> in order to synchronize
   * the slider with the clock's current time.
   */
  private void updateTimeSliderValue() {
    HistoryPlayer historyPlayer;
    try {
      historyPlayer = historyManager.getHistoryPlayer();
    }
    catch (HistoryPlayerNotActiveException e) {
      LOG.debug("Cannot update the time of the slider, because the historyplayer is not available", e);
      return;
    } 
    
    final long start = historyPlayer.getStart().getTime();
    final long end = historyPlayer.getEnd().getTime();
    final long currentTime = historyPlayer.getPlaybackControl().getClockTime();
    
    // Updates the maximum value
    final long timespan = end - start;
    long maximum = (long) (timespan / ((double) SMALLEST_AMOUNT_OF_TIME));
    if (maximum > (long) Integer.MAX_VALUE) {
      maximum = Integer.MAX_VALUE;
    }
    else if (maximum < MINUMUM_VALUE_FOR_THE_MAXIMUM) {
      maximum = MINUMUM_VALUE_FOR_THE_MAXIMUM;
    }
    if (this.timeSlider.getMaximum() != maximum) {
      this.timeSlider.setMaximum((int) maximum);
    }

    // compute the slider position according to the current time
    int n = 0;
    if ((end - start) != 0)
      n = Long.valueOf(((currentTime - start) * this.timeSlider.getMaximum()) / (end - start)).intValue();
    this.timeSlider.setValue(n);
  }
  
  /**
   * This method is basically the counterpart of <code>setValue</code>. It
   * computes the time out of the slider's position. It is usually called to
   * update the clock after the user has moved the slider.
   * 
   * @return The current time
   * @throws HistoryPlayerNotActiveException
   *           if the history player is not available, and the time can
   *           therefore not be calculated
   */
  private long computeTimeFromTimeSlider() throws HistoryPlayerNotActiveException {

    final HistoryPlayer historyPlayer = historyManager.getHistoryPlayer();

    long start = historyPlayer.getStart().getTime();
    long end = historyPlayer.getEnd().getTime();
    int n = this.timeSlider.getValue();

    // compute the current time according to the slider's position
    long time = ((n * (end - start)) / this.timeSlider.getMaximum()) + start;
    return time;
  }

  /**
   * Updates the clock label.
   */
  private void updateClockLabel() {
    HistoryPlayer historyPlayer;
    try {
      historyPlayer = historyManager.getHistoryPlayer();
    }
    catch (HistoryPlayerNotActiveException e) {
      historyPlayer = null;
    }
    
    if (historyPlayer == null) {
      this.clockButton.setText("Uninitialized...");
    }
    else {
      final Long time = historyPlayer.getPlaybackControl().getClockTime();
      if (time < historyPlayer.getStart().getTime()) {
        this.clockButton.setText("Initializing...");
      }
      else {
        this.clockButton.setText(CLOCK_FORMATTER.format(new Date(time)));
      }
    }
    
  }
    
  /**
   * Updates all gui elements that depend on the clock.
   */
  private void updateGuiElements() {
    updateClockLabel();
    updateTimeSliderValue();
  }
}
