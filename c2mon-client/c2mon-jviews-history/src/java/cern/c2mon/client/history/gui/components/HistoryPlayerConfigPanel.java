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

package cern.c2mon.client.history.gui.components;

import java.awt.BorderLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JPanel;

/**
 * This panel is used for a history player configuration dialog in order to
 * allow the user to select start and end date for the history player.
 * 
 * @author Michael Berberich
 * @see MenuManager
 */
public class HistoryPlayerConfigPanel extends JPanel {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = 930398034506967401L;

  /** The margins for the components */
  private static final int PANEL_MARGIN = 12;
  
  /** The amount of milliseconds that can be retrieved from history from today */
  private static final long HISTORY_TIME = 1L * 1000 * 60 * 60 * 24 * 30;
  
  /** The panel where the user can choose the date and time */
  private final TimeSpanChooser chooser; 
  
  /**
   * Constructor
   */
  public HistoryPlayerConfigPanel() {
    super(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    
    chooser = new TimeSpanChooser();
    chooser.setDateFormatString("dd.MM.yyyy HH:mm:ss");
    
    this.add(chooser, BorderLayout.CENTER);
    
    resetDates();
  }

  /**
   * Resets the dates and the selectable range
   */
  public final void resetDates() {
    final Calendar startDate = Calendar.getInstance();
    startDate.add(Calendar.HOUR_OF_DAY, -1);
    
    setStartDate(startDate.getTime());
    setEndDate(new Date(System.currentTimeMillis()));
    
    final Calendar c = Calendar.getInstance();
    c.setTimeInMillis(System.currentTimeMillis() - HISTORY_TIME);
    c.add(Calendar.DATE, 1);
    c.set(Calendar.HOUR_OF_DAY, 0);
    c.set(Calendar.MINUTE, 0);
    c.set(Calendar.SECOND, 0);
    c.set(Calendar.MILLISECOND, 0);
    
    this.chooser.setSelectableDateRange(
        c.getTime(),
        new Date(System.currentTimeMillis()));
  }

  /**
   * @return The chosen start date
   */
  public Date getStartDate() {
    return this.chooser.getSelectedStartDate();
  }

  /**
   * @return The chosen end date
   */
  public Date getEndDate() {
    return this.chooser.getSelectedEndDate();
  }
  
  /**
   * @param value
   *          the new date to set
   */
  public void setStartDate(final Date value) {
    this.chooser.setSelectedStartDate(value);
  }

  /**
   * @param value
   *          the new date to set
   */
  public void setEndDate(final Date value) {
    this.chooser.setSelectedEndDate(value);
  }
  
  /**
   * 
   * @param enable
   *          <code>true</code> to enable the chooser components,
   *          <code>false</code> to disable
   */
  public void setChoosersEnabled(final boolean enable) {
    this.chooser.setEnabled(enable);
    
  }
}
