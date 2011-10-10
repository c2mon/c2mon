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
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cern.c2mon.client.common.history.SavedHistoryEvent;

/**
 * GUI which represents a {@link SavedHistoryEvent} 
 * 
 * @author vdeila
 */
public class SavedHistoryEventPanel extends JPanel {

  /** serialVersionUID */
  private static final long serialVersionUID = 2485315482068341302L;
  
  /** The name label */
  private final JLabel name;
  
  /** The id label */
  private final JLabel eventId;
  
  /** The description label */
  private final JLabel description;
  
  /** The date label */
  private final JLabel dates;
  
  /** The width of the borders around the labels */
  private final int BORDER_WIDTH = 3;
  
  /** The formatter used to format the from and to dates */
  private final DateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");
  
  /**
   * Constructor
   */
  public SavedHistoryEventPanel() {
    this.setBorder(BorderFactory.createRaisedBevelBorder());
    
    name = new JLabel();
    eventId = new JLabel();
    description = new JLabel();
    dates = new JLabel();
    
    name.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, 0, BORDER_WIDTH));
    eventId.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, 0, BORDER_WIDTH));
    description.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    dates.setBorder(BorderFactory.createEmptyBorder(0, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
    
    final Font currentFont = name.getFont();
    final Map<TextAttribute, Object> fontMap = new Hashtable<TextAttribute, Object>();
    fontMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
    name.setFont(new Font(currentFont.getName(), Font.BOLD, 14).deriveFont(fontMap));
    
    description.setForeground(Color.DARK_GRAY);
    dates.setForeground(Color.DARK_GRAY);
    
    final JPanel north = new JPanel(new BorderLayout());
    north.add(name, BorderLayout.WEST);
    north.add(eventId, BorderLayout.EAST);
    north.setOpaque(false);
    
    setLayout(new BorderLayout());
    add(north, BorderLayout.NORTH);
    add(description, BorderLayout.CENTER);
    add(dates, BorderLayout.SOUTH);
  }
  
  /**
   * @param visible
   *          <code>true</code> to show the details, <code>false</code> to hide
   *          them
   */
  public void setDetailsVisible(final boolean visible) {
    this.description.setVisible(visible);
    this.dates.setVisible(visible);
  }
  
  /**
   * 
   * @param object the object to render
   */
  public void setSavedHistoryEvent(final SavedHistoryEvent event) {
    this.name.setText(event.getName());
    this.description.setText(event.getDescription());
    this.eventId.setText("Id: " + event.getId());
    
    final String startDate;
    final String endDate;
    
    if (event.getStartDate() != null) {
      startDate = dateFormatter.format(event.getStartDate());
    }
    else {
      startDate = "?";
    }
    
    if (event.getEndDate() != null) {
      endDate = dateFormatter.format(event.getEndDate());
    }
    else {
      endDate = "?";
    }
    
    this.dates.setText(String.format("%s to %s", startDate, endDate));
  }
  
}
