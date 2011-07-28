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
package cern.c2mon.client.history.gui.components.design;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalIconFactory;
import javax.swing.plaf.metal.MetalSliderUI;

/**
 * This is a slider which shows a buffering line (like for example like the one
 * on youtube)
 * 
 * @author vdeila
 * 
 */
public class BufferedSliderUI extends MetalSliderUI {

  /**
   * The height of the buffered line
   */
  private static final int BUFFER_TRACK_HEIGHT = 3;
  
  /**
   * The color of the buffered line
   */
  private Color bufferedLineColor = new Color(0x11, 0x00, 0xEE, 0x77);
  /**
   * The color of the bottom border of the buffered line
   */
  private Color bufferedLineButtomBorderColor = new Color(0x11, 0x00, 0xAA, 0x77);
  
  /**
   * How much buffer is loaded, relative to the minimum and maximum of the
   * slider
   */
  private int bufferedValue = 0;
  
  /**
   * Constructor
   */
  public BufferedSliderUI() {
    super();
    
    // This is to make it work also on Mac Os X, see JIRA: TIM-284
    if (UIManager.get("Slider.trackWidth") == null) {
      UIManager.put("Slider.trackWidth", Integer.valueOf(7));
      UIManager.put("Slider.majorTickLength", Integer.valueOf(6));
      UIManager.put("Slider.horizontalThumbIcon", MetalIconFactory.getHorizontalSliderThumbIcon());
      UIManager.put("Slider.verticalThumbIcon", MetalIconFactory.getVerticalSliderThumbIcon());
    }
  }

  @Override
  public void paintTrack(Graphics g) {
    super.paintTrack(g);
    
    // Paints the buffered track
    paintBufferTrack(g);
  }
  
  /**
   * Paints the buffered track
   * 
   * @param g The graphics to draw it on
   */
  public void paintBufferTrack(final Graphics g) {
    if (bufferedValue > 0) {
      g.setColor(this.bufferedLineColor);
      
      final Rectangle trackRect = getBufferTrackRectangle();
      
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        int xPosition = xPositionForValue(this.bufferedValue);
        trackRect.setSize(xPosition - getTrackWidth(), trackRect.height);
      }
      else {
        int yPosition = yPositionForValue(this.bufferedValue);
        trackRect.setSize(trackRect.width, yPosition - getTrackWidth());
      }
      
      g.fillRect(trackRect.x, trackRect.y, trackRect.width, trackRect.height);
      g.setColor(bufferedLineButtomBorderColor);
      g.drawRect(trackRect.x, trackRect.y + trackRect.height - 1, trackRect.width - 1, 1);
    }
  }
  
  /**
   * 
   * @return The rectangle of the buffer track
   */
  protected Rectangle getBufferTrackRectangle() {
    Rectangle rect = getTrackRectangle();
    
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      rect.translate(0, rect.height);
      rect.setSize(rect.width, BUFFER_TRACK_HEIGHT);
    }
    else {
      rect.translate(rect.width, 0);
      rect.setSize(BUFFER_TRACK_HEIGHT, rect.height);
    }
    return rect;
  }
  
  /**
   * 
   * @return The rectangle of the track
   */
  protected Rectangle getTrackRectangle() {
    if (slider.getOrientation() == JSlider.HORIZONTAL) {
      return new Rectangle(
          trackRect.x, 
          trackRect.y + (trackRect.height - getTrackWidth()) / 2, 
          trackRect.width - 1, 
          getTrackWidth());
    }
    else {
      return new Rectangle(
          trackRect.x  + (trackRect.width - getTrackWidth()) / 2, 
          trackRect.y, 
          getTrackWidth(), 
          trackRect.height - 1);
    }
  }

  /**
   * @return the bufferedValue
   */
  public int getBufferedValue() {
    return bufferedValue;
  }

  /**
   * @param bufferedValue The buffered value
   */
  public void setBufferedValue(int bufferedValue) {
    this.bufferedValue = bufferedValue;
  }

  /**
   * @return The color of the buffered line
   */
  public Color getBufferedLineColor() {
    return bufferedLineColor;
  }

  /**
   * @param bufferedLineColor the bufferedLineColor to set
   */
  public void setBufferedLineColor(Color bufferedLineColor) {
    this.bufferedLineColor = bufferedLineColor;
  }

  /**
   * @return The color of the bottom border of the buffered line
   */
  public Color getBufferedLineButtomBorderColor() {
    return bufferedLineButtomBorderColor;
  }

  /**
   * @param bufferedLineButtomBorderColor The color of the bottom border of the buffered line
   */
  public void setBufferedLineButtomBorderColor(Color bufferedLineButtomBorderColor) {
    this.bufferedLineButtomBorderColor = bufferedLineButtomBorderColor;
  }
}
