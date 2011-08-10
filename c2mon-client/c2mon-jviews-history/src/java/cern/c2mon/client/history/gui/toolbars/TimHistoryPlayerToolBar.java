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

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.history.HistoryPlayer;
import cern.c2mon.client.common.history.event.HistoryPlayerAdapter;
import cern.c2mon.client.common.history.event.PlaybackControlAdapter;
import cern.c2mon.client.common.history.event.PlaybackControlListener;
import cern.c2mon.client.common.history.exception.HistoryPlayerNotActiveException;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.history.gui.components.ClockLabel;
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

  /**
   * When moving one tick on the time slider, it moves this amount in time
   * (milliseconds)
   */
  private static final long SMALLEST_AMOUNT_OF_TIME = 1000 * 15;
  
  /** The minumum value to be set for the maximum value. */
  private static final long MINUMUM_VALUE_FOR_THE_MAXIMUM = 80;
  
  /** Log4j logger for this class */
  private static final Logger LOG = Logger.getLogger(TimHistoryPlayerToolBar.class);
  
  /** A clock label that shows the time */
  private ClockLabel clockLabel;

  /** The play/pause button */
  private AbstractButton playButton;

  /** <code>true</code> if the {@link #playButton} is playing (ie. showing the pause sign) */
  private boolean playButtonIsPlaying;
  
  /** The speed button */
  private AbstractButton speedButton;

  /** The popup menu showing the different speeds */
  private JPopupMenu speedPopupMenu;
  
  /** A time slider that indicates the current position of the history player */
  private TimeSlider timeSlider;

  /** A swing timer to update swing components that depend on the clock */
  private Timer timer;
  
  /**
   * The playback control listener is listening to when the player resumes or
   * pauses
   */
  private PlaybackControlListener playbackControlEvents = null;

  /** The timer's update rate */
  private static final int UPDATE_RATE = 90;

  /** The icon for the play button */
  private static final ImageIcon PLAY_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TimHistoryPlayerToolBar.class.getResource("play.gif")));

  /** The icon for the pause button */
  private static final ImageIcon PAUSE_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TimHistoryPlayerToolBar.class.getResource("pause.gif")));

//  /** The icon for the stop button */
//  private static final ImageIcon STOP_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(TimHistoryPlayerToolBar.class.getResource("stop.gif")));
  
  /** When pressing the speed button it uses these intervals */
  private static final double[] CLOCK_SPEEDS = new double[] {
    1.0, 5.0, 10.0, 20.0, 60.0, 5.0 * 60.0, 10.0 * 60.0, 30.0 * 60.0, 60.0 * 60.0, 3.0 * 60.0 * 60.0
  };

  /**
   * Constructor
   */
  public TimHistoryPlayerToolBar() {
    super(HORIZONTAL);
    this.setBorder(BorderFactory.createEmptyBorder());
    this.setOpaque(false);
    
    this.playButton = createButton();
    this.playButton.setIcon(PLAY_ICON);
    this.playButtonIsPlaying = false;
    this.playButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        if (playButtonIsPlaying) {
          pause();
        }
        else {
          play();
        }
      }
    });
    
    this.speedButton = createButton();
    this.speedButton.setHideActionText(false);
    this.speedButton.setPreferredSize(null);
    this.speedButton.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(final MouseEvent e) {
        switch (e.getButton()) {
        case MouseEvent.BUTTON1: // Left mouse button
          // Shows the speed popup menu
          speedPopupMenu.setLocation(speedButton.getLocationOnScreen());
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
    updateSpeedButtonText();
    
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
    speedPopupMenu.setInvoker(this.speedButton);

    this.timeSlider = new TimeSlider();
    this.clockLabel = new ClockLabel();

    this.add(this.playButton);
    this.add(this.speedButton);
    this.add(this.timeSlider);
    this.add(this.clockLabel);

    this.setFloatable(false);
    
    // Install listener
    C2monServiceGateway.getHistoryManager().getHistoryPlayerEvents().addHistoryPlayerListener(new HistoryPlayerEvents());
    
    installBufferedChangeEventListener();
    installTimeSliderListener();
    installUpdateGuiTimer();
  }
  
  /**
   * Creates a simple standardized button
   * 
   * @return A <code>JButton</code>
   */
  private AbstractButton createButton() {
    final AbstractButton button = new JButton();
    button.setMinimumSize(new Dimension(26, 26));
    button.setMaximumSize(new Dimension(26, 26));
    button.setPreferredSize(new Dimension(26, 26));
    button.setHideActionText(true);
    button.setEnabled(true);

    return button;
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
        // Is called when someone tries to change the value to more than is loaded.
        
        try {
          if (C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().isPlaying()) {
            // If the clock is running it is paused
            pause();
          }
          C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().setClockTime(computeTimeFromTimeSlider());
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
        final HistoryPlayer historyPlayer = C2monServiceGateway.getHistoryManager().getHistoryPlayer();
        if (playbackControlEvents == null) {
          playbackControlEvents = new PlaybackControlEvents();
        }
        historyPlayer.getPlaybackControl().addPlaybackControlListener(playbackControlEvents);
        
        updateGuiElements();
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
          final HistoryPlayer historyPlayer = C2monServiceGateway.getHistoryManager().getHistoryPlayer();
          historyPlayer.getPlaybackControl().removePlaybackControlListener(playbackControlEvents);
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
        long historyStartTime = C2monServiceGateway.getHistoryManager().getHistoryPlayer().getStart().getTime();
        final long historyEndTime = C2monServiceGateway.getHistoryManager().getHistoryPlayer().getEnd().getTime();

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
    public void onClockPlaybackSpeedChanged(final double newMultiplier) {
      speedButton.setText(getSpeedString(newMultiplier));
    }

    @Override
    public void onClockTimeChanged(long newTime) {
      // updateClockLabel();
      updateGuiElements();
    }

    @Override
    public void onPlaybackStarting() {
      playButtonIsPlaying = true;
      playButton.setIcon(PAUSE_ICON);
      setTimeSliderEnabled(false);

      timer.start();
    }

    @Override
    public void onPlaybackStopped() {
      playButtonIsPlaying = false;
      playButton.setIcon(PLAY_ICON);
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
                C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().setClockTime(newTime);
                
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
      historyPlayer = C2monServiceGateway.getHistoryManager().getHistoryPlayer();
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

    final HistoryPlayer historyPlayer = C2monServiceGateway.getHistoryManager().getHistoryPlayer();

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
    this.clockLabel.updateTime();
  }
  
  /** Updates the label of the speed button */
  private void updateSpeedButtonText() {
    this.speedButton.setText(getClockSpeedString());
  }

  
  /**
   * Updates all gui elements that depend on the clock.
   */
  private void updateGuiElements() {
    updateClockLabel();
    updateTimeSliderValue();
    updateSpeedButtonText();
  }

  /**
   * This method is called when the user has pressed the pause button.
   */
  private void pause() {
    if (this.playButtonIsPlaying) {
      try {
        C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().pause();
      }
      catch (HistoryPlayerNotActiveException e) {
        LOG.debug("Cannot pause", e);
      } 
    }
  }

  /**
   * This method is called when the user has pressed the play button.
   */
  private void play() {
    if (!this.playButtonIsPlaying) {
      try {
        C2monServiceGateway.getHistoryManager().getHistoryPlayer().getPlaybackControl().resume();
      }
      catch (HistoryPlayerNotActiveException e) {
        LOG.debug("Cannot resume", e);
      }
    }
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
}
