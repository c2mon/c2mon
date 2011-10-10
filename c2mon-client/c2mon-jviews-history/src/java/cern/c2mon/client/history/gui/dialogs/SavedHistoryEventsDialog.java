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
package cern.c2mon.client.history.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import cern.c2mon.client.common.history.SavedHistoryEvent;
import cern.c2mon.client.history.dbaccess.beans.SavedHistoryEventBean;
import cern.c2mon.client.history.gui.components.SavedHistoryEventCellRenderer;
import cern.c2mon.client.history.gui.components.model.SearchListModel;

/**
 * Dialog where the user can select a saved history event. It also contains a
 * search box.
 * 
 * @author vdeila
 * 
 */
public class SavedHistoryEventsDialog {
  
  /** serialVersionUID */
  private static final long serialVersionUID = -3770153663951966645L;
  
  /** The parent component of the dialog */
  private final Component parent;  
  
  /** The panel */
  private final JPanel panel;
  
  /** The list of events */
  private final JList eventList;
  
  /** The search text field */
  private final JTextField searchField;
  
  /** The list model */
  private final SearchListModel<String> searchListModel;
  
  /** The margin used between the components */
  private static final int PANEL_MARGIN = 12;
  
  /**
   * @param parent
   *          the parent component
   * @param listData
   *          the data which the user can select from
   */
  public SavedHistoryEventsDialog(final Component parent, final Object[] listData) {
    this.parent = parent;

    this.searchListModel = new SearchListModel<String>(listData, new SavedHistoryEventSearchMatcher());
    this.eventList = new JList(this.searchListModel);
    this.eventList.setCellRenderer(new SavedHistoryEventCellRenderer());
    this.eventList.setVisibleRowCount(7);
    this.eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    
    this.eventList.setPrototypeCellValue(
        new SavedHistoryEventBean(-1, 
            "Dummy - Prototype", 
            "This is a prototype cell value to determine the width and height of the cells",
            new Date(), new Date()));
    
    this.searchField = new JTextField();
    
    final JPanel searchPanel = new JPanel(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    searchPanel.add(new JLabel("Search: "), BorderLayout.WEST);
    searchPanel.add(this.searchField, BorderLayout.CENTER);
    
    final JPanel listPanel = new JPanel(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    listPanel.add(new JLabel("Please select a saved event"), BorderLayout.NORTH);
    listPanel.add(new JScrollPane(this.eventList), BorderLayout.CENTER);
    
    this.panel = new JPanel(new BorderLayout(PANEL_MARGIN, PANEL_MARGIN));
    this.panel.add(searchPanel, BorderLayout.NORTH);
    this.panel.add(listPanel, BorderLayout.CENTER);
    
    this.searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override
      public void removeUpdate(DocumentEvent e) {
        searchListModel.search(searchField.getText());
      }
      @Override
      public void insertUpdate(DocumentEvent e) {
        searchListModel.search(searchField.getText());
      }
      @Override
      public void changedUpdate(DocumentEvent e) {
        searchListModel.search(searchField.getText());
      }
    });
    
  }
  
  /**
   * 
   * @param selectedObject the object to select in the list
   */
  public void setSelectedObject(final Object selectedObject) {
    if (selectedObject != null) {
      this.eventList.setSelectedValue(selectedObject, true);
    }
    else {
      this.eventList.setSelectedIndex(0);
    }
  }
  
  /**
   * 
   * @return the selected value, if it is a {@link SavedHistoryEvent}
   */
  public SavedHistoryEvent getSelectedSavedHistoryEvent() {
    final Object selected = getSelectedObject();
    if (selected instanceof SavedHistoryEvent) {
      return (SavedHistoryEvent) selected;
    }
    return null;
  }
  
  /**
   * @return the selected value, if it is a {@link String}
   */
  public String getSelectedString() {
    final Object selected = getSelectedObject();
    if (selected instanceof String) {
      return (String) selected;
    }
    return null;
  }
  
  /**
   * @return the selected object
   */
  public Object getSelectedObject() {
    return eventList.getSelectedValue();
  }
  
  /**
   * Shows the dialog
   * 
   * @return <code>true</code> if Ok were pressed, <code>false</code> otherwise
   */
  public boolean showDialog() {
    final int result = JOptionPane.showConfirmDialog(
        parent, 
        panel, 
        "Saved history events", 
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE);

    return result == JOptionPane.OK_OPTION;
  }

  
  public static void main(String[] args) throws ParseException {
    final Object[] beans = new Object[] {
        "None",
        new SavedHistoryEventBean(7L, "Thunderstorm cuts 22/06/11", "Thunderstorm cuts LHC 18kV loop at 16:17 (log 2011/06/22 16:00:00 <-> 2011/06/23 08:00:00)", DateFormat.getInstance().parse("02.12.2010 08:25:00"), DateFormat.getInstance().parse("02.12.2010 09:40:00")),
        new SavedHistoryEventBean(10L, "Major Event 1160781", "Short circuit ME4, Major Event 1160781", DateFormat.getInstance().parse("11.09.2011 10:30:00"), DateFormat.getInstance().parse("11.09.2011 20:00:00")),
        new SavedHistoryEventBean(3L, "TEST 7/12/2010: 8h25 - 9h40", "Tests between 8h25 - 9h40", DateFormat.getInstance().parse("07.12.2010 08:25:00"), DateFormat.getInstance().parse("07.12.2010 09:40:00")),
        new SavedHistoryEventBean(4L, "TEST 7/12/2010: 10h15 - 11h15", "Tests between 10h15 - 11h15", DateFormat.getInstance().parse("07.12.2010 10:15:00"), DateFormat.getInstance().parse("07.12.2010 11:15:00")),
        new SavedHistoryEventBean(5L, "TEST 7/12/2010: 13h20 - 16h", "Tests between 13h20 -16h00", DateFormat.getInstance().parse("07.12.2010 13:20:00"), DateFormat.getInstance().parse("07.12.2010 16:00:00")),
        new SavedHistoryEventBean(1L, "Peter Sollander May power-cut", "power cut", DateFormat.getInstance().parse("28.05.2010 21:00:00"), DateFormat.getInstance().parse("30.05.2010 00:00:00")),
        new SavedHistoryEventBean(6L, "Power Cut 18/12/2010", "Power cut on 18th December 2010 11:30AM - 6:30 PM", DateFormat.getInstance().parse("18.12.2010 11:30:00"), DateFormat.getInstance().parse("18.12.2010 18:30:00")),
        new SavedHistoryEventBean(2L, "TEST of power cut on 9/12/2010", "Power cut test at CERN on 09 December 2010 , start time 7am", DateFormat.getInstance().parse("09.12.2010 06:50:00"), DateFormat.getInstance().parse("09.12.2010 10:00:00")),
        new SavedHistoryEventBean(8L, "Thunderstorms on 10 July 2011", "Triple perturbations from thunderstorms", DateFormat.getInstance().parse("10.07.2011 11:00:00"), DateFormat.getInstance().parse("10.07.2011 23:00:00")),
        new SavedHistoryEventBean(9L, "MP7 cut by pick axe work", "MP7 cut by pick axe work", DateFormat.getInstance().parse("18.08.2011 11:30:00"), DateFormat.getInstance().parse("18.08.2011 23:00:00"))
    };
    
    new SavedHistoryEventsDialog(null, beans).showDialog();
  }
}
