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

package cern.c2mon.client.video.viewers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JPanel;

import uk.org.netvu.control.ControlSet;
import uk.org.netvu.swing.JLayoutComboBox;
import uk.org.netvu.swing.JVideoButtons;
import uk.org.netvu.swing.JViewer;
import cern.c2mon.client.video.IVideoViewer;
import cern.c2mon.client.video.IVideoViewerManager;
import cern.c2mon.client.video.managers.NetvuViewerManager;


/**
 * This extended JPanel class provides a Video viewer and creates therefore all
 * needed objects. Please note that the viewer is controlled by its manager instance.
 * <br><br>
 * The VideoPanel can be seen as a wrapper around the JViewer from NetVue. This wrapper
 * class first of all hides the complexity of JViewer and adapts its behaviour to the
 * needs of the TIM Viewer video supply.
 * 
 * @author Matthias Braeger
 */
public class NetvuViewer extends JPanel implements IVideoViewer {

  /** The serial Version UID of the class. Needed for Java 1.6 */
  private static final long serialVersionUID = -4913858734982183463L;
  
  /** No signal image */
  public static final Image NO_SIGNAL_IMG = 
    Toolkit.getDefaultToolkit().getImage(NetvuViewer.class.getResource("no_signal.jpg"));;
  
  /** Control set of the video viewer */
  private ControlSet controlSet = null;
  
  /** Is used to change the layout style */
  private JLayoutComboBox layoutCombo = null;
  
  /** Controls the view and handles all information about the images */
  private final NetvuViewerManager manager;
  
  /** The information label */
  private JLabel titleLabel = null;
  
  /**
   * Default Constructor. <br>
   * It generates a VideoPanel with 4 video screens and without
   * controls.
   */
  public NetvuViewer() {
    this(false);
  }
  
  /**
   * Constructor that creates a VideoPanel with 4 video
   * screens or only a single screen view. The panel will contain no controls
   * @param singleLayoutEnabled If true, then is creates only a single screen view
   */
  public NetvuViewer(final boolean singleLayoutEnabled) {
    this(singleLayoutEnabled, false);
  }
  
  /**
   * Constructor that creates a VideoPanel with 4 video
   * screens or only a single screen view. The panel will contain no controls but
   * an information label to display additional information, if wished.
   * @param singleLayoutEnabled If true, then is creates only a single screen view
   * @param informationEnabled If true, then an additional JLabel field will be shown
   */
  public NetvuViewer(final boolean singleLayoutEnabled, final boolean informationEnabled) {
    this(singleLayoutEnabled, informationEnabled, false);
  }
  
  /**
   * Constructor that creates a VideoPanel with 4 video
   * screens or only a single screen view. Additionally it can provide a selection
   * combo box so that the user can later change the layout of the videoPanel. 
   * An information label can be shown to display additional information, if wished.
   * @param singleLayoutEnabled If true, then is creates only a single screen view
   * @param informationEnabled If true, then an additional JLabel field will be shown
   * @param layoutCtrlEnabled If true, then the user has the possibility to change the layout
   */
  public NetvuViewer(final boolean singleLayoutEnabled, 
                     final boolean informationEnabled, 
                     final boolean layoutCtrlEnabled) {
    this(singleLayoutEnabled, informationEnabled, layoutCtrlEnabled, false);
  }
  
  /**
   * Constructor that creates a VideoPanel with 4 video
   * screens or only a single screen view. Additionally it can provide a selection
   * combo box so that the user can later change the layout of the videoPanel.
   * An information label can be shown to display additional information, if wished.
   * @param singleLayoutEnabled If true, then is creates only a single screen view
   * @param informationEnabled If true, then an additional JLabel field will be shown
   * @param layoutCtrlEnabled If true, then the user has the possibility to change the layout
   * @param controlButtonsEnabled true, if Video Panel should have control buttons to 
   * pause or rewind a video stream
   */
  public NetvuViewer(final boolean singleLayoutEnabled,
                     final boolean informationEnabled,
                     final boolean layoutCtrlEnabled,
                     final boolean controlButtonsEnabled) {
    super(new BorderLayout());
    
    this.manager = new NetvuViewerManager(singleLayoutEnabled);
      
    createViewer(singleLayoutEnabled, informationEnabled);
    
    if ( layoutCtrlEnabled )
      // Puts the control panel to the SOUTH
      createControlPanel(controlButtonsEnabled);
  }
  
  /**
   * @return the manager of this viewer.
   */
  @Override
  public final IVideoViewerManager getVideoViewerManager() {
    return manager;
  }
  
  /**
   * Sets the information text
   * @param text The information text
   */
  @Override
  public final void setTitle(final String text) {
    titleLabel.setText(text.toUpperCase());
  }
  
  /**
   * Returns the current height of the title label 
   * This method is preferable to writing component.getBounds().height, or component.getSize().height 
   * because it doesn't cause any heap allocations.
   * 
   * @return the current height of the title label
   */
  @Override
  public final int getTitleLabelHeight() {
    return titleLabel.getHeight();
  }
  
  /**
   * Creates an Information label that is placed right over the Video Viewer
   */
  private void createTitleLabel() {
    titleLabel = new JLabel(" ", JLabel.CENTER);
    Font font = new Font("Arial", Font.BOLD, 14);
    titleLabel.setForeground(Color.BLUE);
    titleLabel.setFont(font);
  }
  
  /**
   * This function creates the lower control panel
   * @param controlButtonsEnabled true, if button panel shall be enabled
   */
  private void createControlPanel(final boolean controlButtonsEnabled) {
    final JPanel ctrlPanel = new JPanel(new FlowLayout());
    
    
    
    layoutCombo = new JLayoutComboBox();
    // This sets the sdk to only add a single camera from each server
    layoutCombo.setAutoFillOnChange(false);
    layoutCombo.setControlSet(controlSet);
    
    ctrlPanel.add(layoutCombo);
    if ( controlButtonsEnabled ) {
      // Contains the control buttons for the video image
      final JVideoButtons controlButtons = new JVideoButtons();
      controlButtons.setControlSet(controlSet);
      ctrlPanel.add(controlButtons);
    }

    add(ctrlPanel, BorderLayout.SOUTH);
  }
  
  /**
   * Creates a new JViewer and control set to display a
   * video stream.
   * @param singleLayoutActivated true, if single layout shall be activated.
   * @param titleEnabled true, if title panel shall be visible.
   */
  private void createViewer(final boolean singleLayoutActivated, final boolean titleEnabled) {
    final JViewer viewer = manager.getViewer();
    // Allows the user to select a segment in the viewer
    viewer.setSelectActivePosition(true);
    // prevents the user to deselect a segment in the viewer
    viewer.setUnselectActivePosition(false);
    viewer.setMaximumSize(new Dimension(2147483647, 2147483647));
    viewer.setTimeoutImage(NO_SIGNAL_IMG);
    viewer.setIdleImage(NO_SIGNAL_IMG);
    viewer.setTitlesUse24HourFormat(true);
    // Listen to changes on the camera selection.
    viewer.addPropertyChangeListener(NetvuViewerManager.CAMERA_EVENT, manager);
    // Listen to changes in the active position selection
    viewer.addPropertyChangeListener(NetvuViewerManager.POSITION_EVENT, manager);
    
    controlSet = new ControlSet();
    controlSet.setViewerControl(viewer);
    
    if ( titleEnabled ) {
      JPanel panel = new JPanel(new BorderLayout());
      panel.add(viewer, BorderLayout.CENTER);
      createTitleLabel();
      panel.add(titleLabel, BorderLayout.NORTH);
      panel.add(viewer, BorderLayout.CENTER);
      this.add(panel, BorderLayout.CENTER);
    }
    else 
      this.add(viewer, BorderLayout.CENTER);
  }

  @Override
  public Component getVideoViewerComponent() {
    return this;
  }
}
