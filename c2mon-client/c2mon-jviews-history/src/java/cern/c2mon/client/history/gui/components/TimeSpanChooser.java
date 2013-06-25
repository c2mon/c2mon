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
package cern.c2mon.client.history.gui.components;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import cern.c2mon.client.ext.history.common.Timespan;
import cern.c2mon.client.history.gui.components.event.TimeSpanChooserListener;

import com.toedter.calendar.JDateChooser;

/**
 * A GUI for requesting a {@link Timespan} from the user.
 * 
 * @author vdeila
 * 
 */
public class TimeSpanChooser extends JPanel {

  /** serialVersionUID */
  private static final long serialVersionUID = 5586451160333467854L;

  /** The margins for the components */
  private static final int PANEL_MARGIN = 12;

  /** The start date chooser */
  private JDateChooser startDateChooser;

  /** The end date chooser */
  private JDateChooser endDateChooser;
  
  /** Whether the time span is valid or not */
  private boolean valid;
  
  /** <code>true</code> when events should be ignored */
  private boolean ignoreEvents = false;
  
  /** List of listeners */
  private final List<TimeSpanChooserListener> listeners; 
  
  /**
   * Constructor
   */
  public TimeSpanChooser() {
    super(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));

    this.listeners = new ArrayList<TimeSpanChooserListener>();
    
    this.startDateChooser = new JDateChooser(new Date(System.currentTimeMillis()));
    this.endDateChooser = new JDateChooser(new Date(System.currentTimeMillis()));
    this.valid = true;
    
    final JPanel labelPanel = new JPanel(new GridLayout(2, 1, PANEL_MARGIN, PANEL_MARGIN));
    labelPanel.add(new JLabel("Start date:"));
    labelPanel.add(new JLabel("End date:"));

    final JPanel dateChooserPanel = new JPanel(new GridLayout(2, 1, PANEL_MARGIN, PANEL_MARGIN));    
    dateChooserPanel.add(startDateChooser);
    dateChooserPanel.add(endDateChooser);

    this.add(labelPanel, BorderLayout.WEST);
    this.add(dateChooserPanel, BorderLayout.CENTER);
    
    final DateValidator dateValidator = new DateValidator();
    this.startDateChooser.addPropertyChangeListener(dateValidator);
    this.endDateChooser.addPropertyChangeListener(dateValidator);
  }
  
  /**
   * Makes the time span valid
   */
  class DateValidator implements PropertyChangeListener {
    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
      if (ignoreEvents) {
        return;
      }
      valid = startDateChooser.getDate().compareTo(endDateChooser.getDate()) <= 0;
      if (!valid) {
        if (evt.getSource() == startDateChooser) {
          endDateChooser.setDate(startDateChooser.getDate());
        }
        else {
          startDateChooser.setDate(endDateChooser.getDate());
        }
      }
      
      if (evt.getPropertyName().equalsIgnoreCase("date")
          && evt.getNewValue() != null
          && evt.getNewValue() instanceof Date) {
        if (evt.getSource() == startDateChooser) {
          for (TimeSpanChooserListener listener : getTimeSpanChooserListeners()) {
            listener.onSelectedStartDateChanged((Date) evt.getNewValue());
          }
        }
        else if (evt.getSource() == endDateChooser) {
          for (TimeSpanChooserListener listener : getTimeSpanChooserListeners()) {
            listener.onSelectedEndDateChanged((Date) evt.getNewValue());
          }
        }
      }
    }
  }
  
  @Override
  public void setEnabled(final boolean enabled) {
    this.ignoreEvents = true;
    if (startDateChooser != null) {
      startDateChooser.setEnabled(enabled);
      endDateChooser.setEnabled(enabled);
    }
    super.setEnabled(enabled);
    this.ignoreEvents = false;
  }

  /**
   * @param dateFormat
   *          the format to use for the date and time, for example
   *          <code>dd.MM.yyyy HH:mm:ss</code>
   */
  public void setDateFormatString(final String dateFormat) {
    this.ignoreEvents = true;
    this.startDateChooser.setDateFormatString(dateFormat);
    this.endDateChooser.setDateFormatString(dateFormat);
    this.ignoreEvents = false;
  }

  /**
   * @return the date-time format that is used for the date and time, for
   *         example <code>dd.MM.yyyy HH:mm:ss</code>
   */
  public String getDateFormatString() {
    return startDateChooser.getDateFormatString();
  }

  /**
   * @param from
   *          the earliest date which can be selected
   * @param to
   *          the latest date which can be selected
   */
  public void setSelectableDateRange(final Date from, final Date to) {
    setSelectableStartDateRange(from, to);
    setSelectableEndDateRange(from, to);
  }

  /**
   * Set the selectable date range for the start date
   * 
   * @param from
   *          the earliest date which can be selected
   * @param to
   *          the latest date which can be selected
   */
  public void setSelectableStartDateRange(final Date from, final Date to) {
    this.ignoreEvents = true;
    this.startDateChooser.setSelectableDateRange(from, to);
    this.ignoreEvents = false;
  }

  /**
   * Set the selectable date range for the end date
   * 
   * @param from
   *          the earliest date which can be selected
   * @param to
   *          the latest date which can be selected
   */
  public void setSelectableEndDateRange(final Date from, final Date to) {
    this.ignoreEvents = true;
    this.endDateChooser.setSelectableDateRange(from, to);
    this.ignoreEvents = false;
  }

  /**
   * @param timespan
   *          the time span to select
   */
  public void setSelectedTimeSpan(final Timespan timespan) {
    setSelectedStartDate(timespan.getStart());
    setSelectedEndDate(timespan.getEnd());
  }
  
  /**
   * @return the time span which is selected
   */
  public Timespan getSelectedTimeSpan() {
    return new Timespan(
        this.startDateChooser.getDate(),
        this.endDateChooser.getDate());
  }
  
  /** 
   * @param start the start date to select
   */
  public void setSelectedStartDate(final Date start) {
    this.ignoreEvents = true;
    this.startDateChooser.setDate(start);
    this.ignoreEvents = false;
  }
  
  /** 
   * @param end the end date to select
   */
  public void setSelectedEndDate(final Date end) {
    this.ignoreEvents = true;
    this.endDateChooser.setDate(end);
    this.ignoreEvents = false;
  }
  
  /**
   * @return the selected start date
   */
  public Date getSelectedStartDate() {
    return this.startDateChooser.getDate();
  }
  
  /**
   * @return the selected end date 
   */
  public Date getSelectedEndDate() {
    return this.endDateChooser.getDate();
  }

  /**
   * @return whether the time span is valid or not. <code>false</code> if the
   *         end date is before the start date
   */
  public boolean isDatesValid() {
    return valid;
  }

  //
  // Methods for adding / removing listeners
  //

  /**
   * 
   * @param listener
   *          the listener to add
   */
  public synchronized void addTimeSpanChooserListener(final TimeSpanChooserListener listener) {
    this.listeners.add(listener);
  }
  
  /**
   * 
   * @param listener
   *          the listener to remove
   */
  public synchronized void removeTimeSpanChooserListener(final TimeSpanChooserListener listener) {
    this.listeners.remove(listener);
  }
  
  private synchronized Collection<TimeSpanChooserListener> getTimeSpanChooserListeners() {
    return new ArrayList<TimeSpanChooserListener>(this.listeners);
  }

}
