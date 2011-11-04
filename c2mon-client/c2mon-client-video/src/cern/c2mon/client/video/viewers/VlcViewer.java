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
package cern.c2mon.client.video.viewers;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cern.c2mon.client.video.IVideoViewer;
import cern.c2mon.client.video.IVideoViewerManager;
import cern.c2mon.client.video.managers.VlcViewerManager;

/**
 * The VlcViewer is using JVLC to communicate and control the native VLC
 * libraries. The VLC image is the redirected to a <code>Canvas</code>.
 * @author Matthias Braeger
 *
 */
public class VlcViewer extends JPanel implements IVideoViewer {

  /** Serial Version UID */
  private static final long serialVersionUID = 4362746510144111957L;
  
  /** The title label which is placed right over the viewer. */
  private final JLabel titleLabel;
  
  /** The manager of this view */
  private final VlcViewerManager manager;

  /**
   * Default Constructor
   */
  public VlcViewer() {
    super(new BorderLayout());
    
    // Create the manager
    manager = new VlcViewerManager();
    
    // Create the title label
    titleLabel = new JLabel(" ", JLabel.CENTER);
    Font font = new Font("Arial", Font.BOLD, 14);
    titleLabel.setForeground(Color.BLUE);
    titleLabel.setFont(font);
    this.add(titleLabel, BorderLayout.NORTH);
    
    createViewerPanel();
  }
  
  /**
   * Private method to create the viewer panel that will display the
   * video streams.
   */
  private void createViewerPanel() {
    // Add viewer Canvas into the CENTER
    final JPanel viewerPanel = new JPanel(new BorderLayout());
    viewerPanel.setBorder(BorderFactory.createEmptyBorder());
    
    final Canvas viewerCanvas = manager.getViewerCanvas();
    viewerCanvas.setBackground(Color.BLACK);
    
    viewerPanel.add(viewerCanvas, BorderLayout.CENTER);
    this.add(viewerPanel, BorderLayout.CENTER);
  }
  

  @Override
  public int getTitleLabelHeight() {
    return titleLabel.getHeight();
  }


  @Override
  public Component getVideoViewerComponent() {
    return this;
  }


  @Override
  public IVideoViewerManager getVideoViewerManager() {
    return manager;
  }


  @Override
  public void setTitle(String text) {
    titleLabel.setText(text);
  }
}
