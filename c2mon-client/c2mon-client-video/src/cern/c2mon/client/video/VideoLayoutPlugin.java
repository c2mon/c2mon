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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import uk.org.netvu.awt.DefaultLayoutPlugin;
import uk.org.netvu.control.LayoutPlugin;
import uk.org.netvu.control.ViewerControl;
import uk.org.netvu.control.ViewerPanel;
import uk.org.netvu.swing.JViewer;


/**
 * This class is mainly taken from the the tutorial from NetVue. It is
 * responsible for changing the layout of the VideoViewer.
 * @author Matthias Braeger
 *
 */
public class VideoLayoutPlugin implements LayoutPlugin {

  /** String to represent the Single layout. */
  public static final String SINGLE = "Single";
  /** String to represent the Four way layout. */
  public static final String FOUR_WAY = "Four Way";
  
  /**
   * Icon to represent the Single layout. NB, we borrow the icon from the
   * DefaultLayoutPlugin.
   */
  private static final Image SINGLE_ICON = Toolkit.getDefaultToolkit()
    .getImage(DefaultLayoutPlugin.class.getResource("single.gif"));
  
  /**
   * Icon to represent the Four way layout. NB, we borrow the icon from the
   * DefaultLayoutPlugin.
   */
  private static final Image FOUR_WAY_ICON = Toolkit.getDefaultToolkit()
    .getImage(DefaultLayoutPlugin.class.getResource("four_way.gif"));
  /** A private field which used to rememeber which layout is current. */
  private String layout = SINGLE;
  /** The Viewer or JViewer that this layoutPlugin controls the Layout for. */
  private ViewerControl viewerControl = null;
  /** The JViewer that the viewerControl uses to delegate jobs to. */
  private JViewer lightweightViewer = null;
  /** The ViewerPanels that this LayoutPlugin will arrange. */
  private ViewerPanel[] viewerPanels = null;
  /** The largest number of ViewerPanels being displayed in the current Layout. */
  private int maxPosition = 1;
  private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
  
  /**
   * Default Constructor
   * @param viewerControl
   */
  public VideoLayoutPlugin(ViewerControl viewerControl) {
    this.viewerControl = viewerControl;
    lightweightViewer = findLightweightViewer(viewerControl);
  }
  
  /**
   * This private method will attempt to find the JViewer that a
   * ViewerControl should have.
   * 
   * @param viewerControl
   *            This is the JViewer/Viewer for which to find the
   *            JViewer.
   * @returns A reference to the JViewer should there be one,
   *          otherwise null.
   */
  private JViewer findLightweightViewer(final ViewerControl viewerControl) {
    // If the ViewerControl is a JViewer, then we're done.
    if (viewerControl instanceof JViewer) {
      return ((JViewer) viewerControl);
    }
    JViewer lightweightViewer = null;
    // Otherwise, get the components of the ViewerControl (which will be a
    // JViewer or Viewer)...
    Component[] components = ((Container) viewerControl).getComponents();
    // ...and search through them for the real JViewer.
    for (int c = 0; c < components.length; c++) {
      if (components[c] instanceof JViewer)
        lightweightViewer = (JViewer) components[c];
    }
    return lightweightViewer;
  }
  
  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    changeSupport.addPropertyChangeListener(pcl);
  }
  
  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    changeSupport.removePropertyChangeListener(pcl);
  }
  
  public int getMaxPosition() {
    return maxPosition;
  }
  
  public ViewerPanel getPosition(int position) {
    return viewerPanels[position];
  }
  
  /**
   * This is the method that is called to ask for a particular layout to be
   * set up.
   * @param layout The video layout which is either SINGLE of FOUR_WAY.
   */
  public final void setLayout(final String layout) {
    String oldLayout = this.layout;
    this.layout = layout;
    if (layout.equals(SINGLE))
      setGrid(1, 1);
    else if (layout.equals(FOUR_WAY))
      setGrid(2, 2);
    else {
      this.layout = SINGLE;
      setGrid(1, 1);
    }
    changeSupport.firePropertyChange(LAYOUT_PROPERTY, oldLayout, layout);
    lightweightViewer.validate();
  }
  
  
  /**
   * Handy method to centralize our setting of the layout.
   * 
   * @param rows Amount of rows
   * @param columns amount of columns
   */
  private void setGrid(final int rows, final int columns) {
    // Calculate the number of ViewerPanels required for this layout.
    maxPosition = rows * columns;
    // Remove the ViewerPanels that made up the previous layout.
    lightweightViewer.removeAll();
    // Set the layout to the appropriate grid for the new layout.
    lightweightViewer.setLayout(new GridLayout(rows, columns));
    // Create a new set of ViewerPanels for the new layout.
    viewerPanels = viewerControl.allocateViewerPanels(maxPosition);
    // Add the ViewerPanels as child Components to the JViewer.
    for (int i = 0; i < maxPosition; i++) {
      lightweightViewer.add((Component) viewerPanels[i]);
    }
  }
  
  /**
   * @return The layout
   */
  public final String getLayout() {
    return layout;
  }
  
  /**
   * @return All layouts being set.
   */
  public final String[] getLayouts() {
    String[] layouts = { SINGLE, FOUR_WAY };
    return layouts;
  }
  
  /**
   * @return The layout images being set.
   */
  public final Image[] getLayoutImages() {
    Image[] layouts = { SINGLE_ICON, FOUR_WAY_ICON };
    return layouts;
  }
  
  /**
   * Set the default layout.
   * @param string The default layout.
   */
  public final void setDefaultLayout(final String string) {
    setLayout(string);
  }
  
  /**
   * Get the default layout.
   * @return The default layout 
   */
  public final String getDefaultLayout() {
    return getLayout();
  }
  
  public void setViewerControl(ViewerControl viewerControl) {
    // TODO Auto-generated method stub
  }
}
