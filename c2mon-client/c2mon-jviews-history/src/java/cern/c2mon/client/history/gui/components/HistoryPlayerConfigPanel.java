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
