/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2010 CERN This program is free software; you can redistribute
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
 *****************************************************************************/

package cern.c2mon.client.history.gui.components;


/**
 * This time slider serves as a progress indicator for the history player. It
 * represents a time scale and the current time of the provided clock instance.
 * It allows the user to rewind and forward the history player and therefore
 * changing the current time of the clock.
 * 
 * @author Michael Berberich
 */
public class TimeSlider extends BufferedSlider {

  /** Auto generated serial version UID */
  private static final long serialVersionUID = -9196743332372645804L;
  
  /** The percentage loaded */
  private double percentLoaded = 0;
  
  /**
   * Constructor
   */
  public TimeSlider() {
    super();
    
    this.setValue(0);
    setPercentLoaded(0);
  }
  
  /**
   * Sets how many percent is done buffering
   * 
   * @param value A value between 0.0 and 1.0 
   */
  public void setPercentLoaded(final double value) {
    this.percentLoaded = value;
    
    // Sets the value of the buffering
    setBufferedValue((int) ((getMaximum() - getMinimum()) * value + getMinimum()));
  }
  
  @Override
  public void setMaximum(int maximum) {
    super.setMaximum(maximum);
    setPercentLoaded(this.percentLoaded);
  }

  @Override
  public void setMinimum(int minimum) {
    super.setMinimum(minimum);
    setPercentLoaded(this.percentLoaded);
  }

  @Override
  public void setValue(final int n) {
    super.setValue(n);
  }
  
}
