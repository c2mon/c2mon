/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
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
 ******************************************************************************/
package cern.c2mon.client.video;

import java.awt.Dimension;

import org.apache.log4j.Logger;


/**
 * This class is used to recalculate the location and size of the both dividers
 * that are used within the VideoMainPanel
 * 
 * @author Matthias Braeger
 */
public final class DividerRecalculation {
  
  /** Singleton instance */
  private static DividerRecalculation instance = null;
  
  /** Log4j logger */
  private static final Logger LOG = Logger.getLogger(DividerRecalculation.class);
  
  /** the ratio is equals viewer's height / width */
  private static final double ASPECT_RATIO = 0.75;
  
  /** total height of the VideoMainPanel */
  private static double panelHeight;
  /** total width of the VideoMainPanel */
  private static double panelWidth;
  
  /** 
   * The height of the information label that is used 
   * in the viewer stack. There are as much labels as views
   * in the stack.
   */
  private static int totalQueueTitleLabelsHeight;
  
  /** The queue size */
  private static int videoQueueSize; 
  
  /** The size of "this" divider */
  private static int verticalDividerSize = 10;
  /** The location of "this" divider */
  private static int verticalDividerLocation;
  
  /** The size of the divider between the main viewer and infoPanel*/
  private static int horizontalDividerSize = 10;
  /** The location of the divider between the main viewer and infoPanel*/
  private static int horizontalDividerLocation;
  
  /** Height of a single viewer of the viewer's queue */
  private static double viewerQueueHeight;
  /** Width of a single viewer of the viewer's queue */
  private static double viewerQueueWidth;
  
  /** Width of the main Viewer in the centre */
  private static double centralViewerWidth;
  /** Height of the main Viewer in the centre */
  private static double centralViewerHeight;
  
  
  /**
   * Use this method to get a singleton instance of this class
   * @param panelDim The dimension of the VideoMainPanel
   * @return The singleton instance
   */
  public static DividerRecalculation getInstance(final Dimension panelDim) {
    if ( instance == null ) {
      instance = new DividerRecalculation();
    }
    
    panelHeight = panelDim.getHeight();
    panelWidth = panelDim.getWidth();
    
    verticalDividerSize = 10;
    verticalDividerLocation = 0;
    horizontalDividerLocation = 0;
    horizontalDividerSize = 10;
      
    while (!doCalculations());
    
    return instance;
  }
  
  /**
   * Default Constructor
   */
  private DividerRecalculation() {
    videoQueueSize = 
      (Integer) VideoViewerConfigLoader.getInstance().getPropertyByName(
        VideoPropertyNames.VIDEO_QUEUE_SIZE);
    
    totalQueueTitleLabelsHeight = 
      DefaultMainPanel.getQueueTitleLabelsHeight() * videoQueueSize;
  }
  
  
  /**
   * Calculates the location and size of the both dividers.
   * @return true, if calculations a finished. In case that the
   *         return value is false we have to call the function again.
   */
  private static boolean doCalculations() {
    if (LOG.isDebugEnabled())
      LOG.debug("panel witdh = " + panelWidth + ", height = " + panelHeight);
    boolean calculationsFinished = true;
    // The total gap size between the video viewers
    int gapsSize = (videoQueueSize - 1) * 5;
    
    // We calculate now the height and width of one single queue video viewer
    viewerQueueHeight = (panelHeight - gapsSize - totalQueueTitleLabelsHeight) / videoQueueSize;
    // 3/4 is the aspect ratio of a normal television image
    viewerQueueWidth = viewerQueueHeight / ASPECT_RATIO;
    calculateVerticalDividerLocation();
    
    
    // We have to do the same calculations for the centerSplitPane
    centralViewerWidth = panelWidth - viewerQueueWidth - verticalDividerSize;
    // again we have to respect the TV aspect ration of 3/4
    centralViewerHeight = centralViewerWidth * ASPECT_RATIO;
    calculateHorizontalDividerLocation();
    
    
    // Before we update, we have to check if there would be still enough space for the info panel
    double infoPanelHeight =  calculateInfoPanelHeight();
    if (Math.round(infoPanelHeight) < VideoInformationPanel.getInstance().getMinimumSize().getHeight()) {
      if (LOG.isDebugEnabled())
        LOG.debug("infoPanel height too small");
      // That is too less. In that case we make the vertical divider bigger to get another aspect ratio
      adjustVerticalDividerSize();
      calculationsFinished = false;
    }
    if (Math.round(infoPanelHeight) > VideoInformationPanel.getInstance().getMaximumSize().getHeight()) {
      if (LOG.isDebugEnabled())
        LOG.debug("infoPanel width too high");
      // That is too big, In that case we make the horizontal divider bigger to get another aspect ratio
      adjustHorizontalDividerSize();
      calculationsFinished = false;
    }
    
    return calculationsFinished;
  }
  
  /**
   * Recalculates the size of the main viewer and the divider of the VideoMainPanel
   */
  private static void adjustVerticalDividerSize() {
    centralViewerHeight = panelHeight - DefaultMainPanel.getTitleLabel().getHeight() - horizontalDividerSize - VideoInformationPanel.getInstance().getMinimumSize().getHeight();
    // again we have to respect the TV aspect ration of 3/4
    centralViewerWidth = centralViewerHeight / ASPECT_RATIO;
    verticalDividerSize = (int) Math.round(panelWidth - viewerQueueWidth - centralViewerWidth);
  }
  
  /**
   * Recalculates the size of the main viewer and the horizontal divider
   */
  private static void adjustHorizontalDividerSize() {
    horizontalDividerSize = 
      (int) Math.round(
          panelHeight - centralViewerHeight - DefaultMainPanel.getTitleLabel().getHeight()
          - VideoInformationPanel.getInstance().getMaximumSize().getHeight());
  }
  
  /**
   * Calculates the the height of the InfoPanel
   * @return The height of the InfoPanel
   */
  private static double calculateInfoPanelHeight() {
    return 
      panelHeight - centralViewerHeight
      - DefaultMainPanel.getTitleLabel().getHeight() - horizontalDividerSize;
  }
  
  /**
   * Helper method to calculate the exact vertical divider location
   */
  private static void calculateVerticalDividerLocation() {
    verticalDividerLocation =
      (int) Math.round(panelWidth - viewerQueueWidth - verticalDividerSize);
  }
  
  /**
   * Helper method to calculate the horizontal divider location
   */
  private static void calculateHorizontalDividerLocation() {
    horizontalDividerLocation =
      (int) Math.round(centralViewerHeight + DefaultMainPanel.getTitleLabel().getHeight());
  }

  /**
   * @return the verticalDividerSize
   */
  public int getVerticalDividerSize() {
    return verticalDividerSize;
  }

  /**
   * @return the verticalDividerLocation
   */
  public int getVerticalDividerLocation() {
    return verticalDividerLocation;
  }

  /**
   * @return the horizontalDividerSize
   */
  public int getHorizontalDividerSize() {
    return horizontalDividerSize;
  }

  /**
   * @return the horizontalDividerLocation
   */
  public int getHorizontalDividerLocation() {
    return horizontalDividerLocation;
  }  
}
