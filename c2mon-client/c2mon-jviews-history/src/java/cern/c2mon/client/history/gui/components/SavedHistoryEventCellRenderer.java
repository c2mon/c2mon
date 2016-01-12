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
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import cern.c2mon.client.ext.history.common.SavedHistoryEvent;

/**
 * Panel which represents a {@link SavedHistoryEvent}. Uses the
 * {@link SavedHistoryEventPanel} for the rendering.
 * 
 * @author vdeila
 */
public class SavedHistoryEventCellRenderer implements ListCellRenderer {

  /** Panel which can represent a SavedHistoryEvent */
  private final SavedHistoryEventPanel savedHistoryEventPanel;
  
  /** Panel used for showing just string objects */
  private final JLabel stringLabel;
  
  /** The panel containing the label and a raised bevel border */
  private final JPanel stringPanel;
  
  /** The border for the cells containing only a single string */
  private static final int LABEL_BORDER_WIDTH = 6;
  
  /**
   * Constructor
   */
  public SavedHistoryEventCellRenderer() {
    this.savedHistoryEventPanel = new SavedHistoryEventPanel();
    this.stringLabel = new JLabel();
    this.stringLabel.setHorizontalAlignment(JLabel.CENTER);
    this.stringLabel.setVerticalAlignment(JLabel.CENTER);
    this.stringLabel.setBorder(BorderFactory.createEmptyBorder(LABEL_BORDER_WIDTH, LABEL_BORDER_WIDTH, LABEL_BORDER_WIDTH, LABEL_BORDER_WIDTH));
    
    this.stringPanel = new JPanel();
    this.stringPanel.setLayout(new BorderLayout());
    this.stringPanel.add(this.stringLabel, BorderLayout.CENTER);
    this.stringPanel.setBorder(BorderFactory.createRaisedBevelBorder());
  }
  
  /**
   * 
   * @param object the object to get a panel for
   */
  private Component getComponent(final Object object) {
    if (object instanceof SavedHistoryEvent) {
      final SavedHistoryEvent event = (SavedHistoryEvent) object;
      this.savedHistoryEventPanel.setSavedHistoryEvent(event);
      return this.savedHistoryEventPanel;
    }
    else {
      this.stringLabel.setText(object.toString());
      return this.stringPanel;
    }
  }
  
  @Override
  public Component getListCellRendererComponent(
      final JList list, 
      final Object value, 
      final int index, 
      final boolean isSelected, 
      final boolean cellHasFocus) {

    final Component component = getComponent(value);
    
    if (isSelected) {
      component.setBackground(list.getSelectionBackground());
      component.setForeground(list.getSelectionForeground());
    }
    else {
      component.setBackground(list.getBackground());
      component.setForeground(list.getForeground());
    }

    return component;
  }
  
}
