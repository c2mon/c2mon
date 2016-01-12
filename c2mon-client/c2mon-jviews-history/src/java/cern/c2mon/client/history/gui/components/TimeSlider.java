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
