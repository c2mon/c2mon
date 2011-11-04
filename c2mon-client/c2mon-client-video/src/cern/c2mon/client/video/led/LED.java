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

package cern.c2mon.client.video.led;

import javax.swing.*;
import java.awt.*;

/**
 * Represents on single LED on the LEDnumberPanel
 * @author Matthias Braeger
 */
public class LED extends JComponent {
  /**
   * 
   */
  private static final long serialVersionUID = -8357986923903133688L;
  private Color c, c1, c2;
  private Insets insets;
  private int curWidth, curHeight;
  /** If LED is resized, set calculatePaintArea to true in order to update */
  public boolean calculatePaintArea = true;

  /**
   * Create a LED which switches between red and gray.<BR>
   */
  public LED() {
    this(Color.red, Color.gray);
  }

  /**
   * Create a LED which doesn't blink.<BR>
   * 
   * @param primary
   * @param secondary
   * 
   * @exception None
   */
  public LED(Color primary, Color secondary) {
    super();
    c1 = primary;
    c2 = secondary;
    c = c2;
    setPreferredSize(new Dimension(15, 15));
    setMinimumSize(new Dimension(15, 15));
    repaint();
  }

  public void usePrimary() {
    c = c1;
    repaint();
  }

  public void useSecondary() {
    c = c2;
    repaint();
  }

  /**
   * Repaints the component
   */
  protected void paintComponent(Graphics g) {
    insets = getInsets();
    curWidth = getWidth() - insets.left - insets.right - 1;
    curHeight = getHeight() - insets.top - insets.bottom - 1;

    g.setColor(c);
    g.fillRect(insets.left, insets.top, curWidth, curHeight);
    
    super.paintComponent(g);
  }

}