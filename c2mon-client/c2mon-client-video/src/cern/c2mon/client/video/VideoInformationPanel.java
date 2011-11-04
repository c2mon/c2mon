/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.client.video;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import cern.c2mon.client.video.led.LEDnumberPanel;

/**
 * The information panel consists of the access point information
 * and a counter for the keys taken. The counter can be switched of,
 * in case it's not needed.
 * @author Matthias Braeger
 *
 */
public final class VideoInformationPanel extends JPanel {
  
  /** The Singleton instance of this class */
  private static VideoInformationPanel instance = null;
  
  /** Serial number for Java compiler */
  private static final long serialVersionUID = -475327399018779804L;

  /** Font of the titles */
  private final Font titleFont;
  
  /** Displays the amount of keys that has been taken */
  private LEDnumberPanel keysTakenDisplay = null;
  
  /** Should contain the description of the access point */
  private JTextArea descriptionArea = null;
  
  /**
   * Returns the singleton instance of that class. In case
   * this method is used for the first time it creates an 
   * instance of the VideoInformationPanel with the default
   * constructor
   * @return the singleton instance of that class
   */
  public static VideoInformationPanel getInstance() {
    if (instance == null)
      instance = new VideoInformationPanel();
    
    return instance;
  }
  
  /**
   * Returns the singleton instance of that class. In case
   * this method is used for the first time it creates an 
   * instance of the VideoInformationPanel and creates a
   * key counter, if desired.
   * @param showKeyCounter <code>true</code>, if key counter shall be visible.
   * @return the singleton instance of that class
   */
  public static VideoInformationPanel getInstance(final boolean showKeyCounter) {
    if (instance == null)
      instance = new VideoInformationPanel(showKeyCounter);
    
    return instance;
  }
  
  /**
   * Default Constructor that provides information panel without
   * key counter.
   */
  private VideoInformationPanel() {
    this(false);
  }
  
  /**
   * Constructor
   * @param showKeyCounter <code>true</code>, if key counter shall be visible.
   */
  private VideoInformationPanel(final boolean showKeyCounter) {
    super(new BorderLayout(10, 0));
    this.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
    this.setMaximumSize(new Dimension(5000, 154)); // we want just fix the max height 
    this.setMinimumSize(new Dimension(5000, 154)); // we want just fix the min height
    
    titleFont = new Font("Arial", Font.BOLD, 18);
    
    this.add(createInfoPanel(), BorderLayout.CENTER);
    if (showKeyCounter)
      this.add(createKeyCounterPanel(), BorderLayout.WEST);
  }
  
  /**
   * Private method that creates the key counter panel.
   * @return The key counter panel.
   */
  private JPanel createKeyCounterPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    keysTakenDisplay = new LEDnumberPanel(Color.red, Color.black);
    
    JLabel keyTakenLabel = new JLabel("Key Counter:");
    keyTakenLabel.setFont(titleFont);
    keyTakenLabel.setForeground(Color.blue);
    keyTakenLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
    
    panel.add(keyTakenLabel, BorderLayout.NORTH);
    panel.add(keysTakenDisplay, BorderLayout.CENTER);
    
    return panel;
  }
  
  /**
   * Creates a Panel to display the information about the Access point.
   * @return The information panel.
   */
  private JPanel createInfoPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    
    JLabel titleLabel = new JLabel("Access Point Information:");
    titleLabel.setFont(titleFont);
    titleLabel.setForeground(Color.BLUE);
    titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 0));
    
    Font descriptionFont = new Font("Arial", Font.PLAIN, 60); // 60 max Font without scroll bar
    descriptionArea = new JTextArea();
    descriptionArea.setEditable(false);
    descriptionArea.setFont(descriptionFont);
    descriptionArea.setForeground(Color.GRAY);
    descriptionArea.setBackground(Color.BLACK);
    descriptionArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    JScrollPane scrollPane = new JScrollPane(descriptionArea);
    
    panel.add(titleLabel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    
    return panel;
  }
  
  /**
   * Sets the amount of keys in the display that have been taken.
   * @param keysTaken The amount of keys that have been taken
   */
  public void setKeysTaken(final int keysTaken) {
    if (keysTakenDisplay != null)
      keysTakenDisplay.setDisplayNumber(keysTaken);
  }
  
  /**
   * Sets the description of the access point into the text area.
   * @param accessPointDescription The description text of the access point
   */
  public void setAccessPointDescription(final String accessPointDescription) {
    descriptionArea.setText(accessPointDescription);
  }
  
  /**
   * Reset the counter and the description field
   */
  public void reset() {
    setAccessPointDescription("");
    if (keysTakenDisplay != null)
      keysTakenDisplay.reset();
  }
}
