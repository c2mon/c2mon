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
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

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
  
  /** The start date chooser */
  private JDateChooser startDateChooser;

  /** The end date chooser */
  private JDateChooser endDateChooser;
  
  /**
   * Constructor
   */
  public HistoryPlayerConfigPanel() {
    super(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    
    final Calendar startDate = Calendar.getInstance();
    startDate.add(Calendar.HOUR_OF_DAY, -1);

    createComponents(startDate.getTime(), new Date(System.currentTimeMillis()));
  }

  /**
   * Creates the labels and the calendars components
   * 
   * @param startTime The initial start date
   * @param endTime The initial end date
   */
  private void createComponents(final Date startTime, final Date endTime) {
    final JPanel labelPanel = new JPanel(new GridLayout(2, 1, PANEL_MARGIN, PANEL_MARGIN));
    labelPanel.add(new JLabel("Start date:"));
    labelPanel.add(new JLabel("End date:"));
    
    final JPanel dateChooserPanel = new JPanel(new GridLayout(2, 1, PANEL_MARGIN, PANEL_MARGIN));
    this.startDateChooser = new JDateChooser(startTime);
    this.startDateChooser.setDateFormatString("dd.MM.yyyy HH:mm:ss");
    this.startDateChooser.setSelectableDateRange(new Date(System.currentTimeMillis() - HISTORY_TIME), new Date(System.currentTimeMillis()));
    dateChooserPanel.add(startDateChooser);
    
    this.endDateChooser = new JDateChooser(endTime);
    this.endDateChooser.setDateFormatString("dd.MM.yyyy HH:mm:ss");
    this.endDateChooser.setSelectableDateRange(new Date(System.currentTimeMillis() - HISTORY_TIME), new Date(System.currentTimeMillis()));
    dateChooserPanel.add(endDateChooser);

    this.add(labelPanel, BorderLayout.WEST);
    this.add(dateChooserPanel, BorderLayout.CENTER);
    
    /*
     * Installs the listeners
     */
    this.startDateChooser.addPropertyChangeListener(new PropertyChangeListener() {
      /**
       * Updates the end-value if the start value is after
       */
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        if (endDateChooser.getDate().compareTo(startDateChooser.getDate()) < 0)
          endDateChooser.setDate(startDateChooser.getDate());
      }
    });

    this.endDateChooser.addPropertyChangeListener(new PropertyChangeListener() {
      /**
       * Updates the start-value if the end value is before
       */
      @Override
      public void propertyChange(final PropertyChangeEvent evt) {
        if (startDateChooser.getDate() != null && endDateChooser.getDate() != null && startDateChooser.getDate().compareTo(endDateChooser.getDate()) > 0)
          startDateChooser.setDate(endDateChooser.getDate());
      }
    });
  }

  /**
   * @return The chosen start date
   */
  public Date getStartDate() {
    return this.startDateChooser.getDate();
  }

  /**
   * @return The chosen end date
   */
  public Date getEndDate() {
    return this.endDateChooser.getDate();
  }
}
