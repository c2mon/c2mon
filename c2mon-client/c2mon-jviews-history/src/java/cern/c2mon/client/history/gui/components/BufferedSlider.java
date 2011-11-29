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

import javax.swing.BoundedRangeModel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cern.c2mon.client.history.gui.components.design.BufferedSliderUI;
import cern.c2mon.client.history.gui.components.event.BufferedChangeListener;

/**
 * This is a slider which have buffering with the slider. (Like on YouTube)
 * 
 * @author vdeila
 * 
 */
public class BufferedSlider extends JSlider {
  
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1874211410403108942L;

  /**
   * The time track ui element
   */
  private BufferedSliderUI bufferedSliderUI;
  
  public BufferedSlider() {
    super();
    initialize();
  }

  public BufferedSlider(int orientation) {
    super(orientation);
    initialize();
  }

  public BufferedSlider(BoundedRangeModel brm) {
    super(brm);
    initialize();
  }

  public BufferedSlider(int min, int max) {
    super(min, max);
    initialize();
  }

  public BufferedSlider(int min, int max, int value) {
    super(min, max, value);
    initialize();
  }

  public BufferedSlider(int orientation, int min, int max, int value) {
    super(orientation, min, max, value);
    initialize();
  }
  
  /**
   * Initializes
   */
  private void initialize() {
    // Uses the buffered slider ui to show how much data is loaded from history
    this.bufferedSliderUI = new BufferedSliderUI();
    this.setUI(this.bufferedSliderUI);
    this.setBufferedValue(0);
  }
  
  /**
   * Adds a BufferedChangeListener to the slider.
   * 
   * @param l The listener to add
   */
  public void addChangeListener(final BufferedChangeListener l) {
    super.addChangeListener(l);
  }
  
  /**
   * Removes a BufferedChangeListener from the slider.
   * 
   * @param l The listener to remove
   */
  public void removeChangeListener(final BufferedChangeListener l) {
    super.removeChangeListener(l);
  }

  /**
   * Fire the valueForced(ChangeEvent) on all listeners
   * 
   * @param source The Object that is the source of the event
   */
  public void fireValueForced(final Object source) {
    for (ChangeListener listener : super.getChangeListeners()) {
      if (listener instanceof BufferedChangeListener) {
        final BufferedChangeListener bufferedChangeListener = (BufferedChangeListener) listener;
        bufferedChangeListener.valueForced(new ChangeEvent(source));
      }
    }
  }
  
  /**
   * If <code>n</code> is beyond what is loaded, it will be set the pointer to as
   * much as is loaded
   * 
   * @param newValue
   *          The new value to set
   */
  @Override
  public void setValue(final int newValue) {
    if (newValue > bufferedSliderUI.getBufferedValue()) {
      // If the slider is moved beyond what is loaded
      
      this.setValue(bufferedSliderUI.getBufferedValue());
      
      // Tells the listeners about the event
      fireValueForced(this);
    }
    else {
      if (newValue != getValue()) {
        super.setValue(newValue);
      }
    }
  }
  
  /**
   * 
   * @param newBufferedValue How far the buffer is loaded
   */
  public void setBufferedValue(final int newBufferedValue) {
    int newValue = newBufferedValue;
    if (newValue < 0) {
      newValue = 0;
    }
    boolean isValueForced = false;
    synchronized (this) {
      double oldBufferedValue = bufferedSliderUI.getBufferedValue();
      this.bufferedSliderUI.setBufferedValue(newValue);
      
      // If the new value is less than the old value the time slider thumb must
      // be checked if it is in a legal position
      if (newValue < oldBufferedValue && getValue() > newValue) {
        this.setValueIsAdjusting(true);
        
        // If the slider is beyond what is loaded
        super.setValue(newValue);
        
        this.setValueIsAdjusting(false);
        
        isValueForced = true;
      }
    
      // Limits the thumb to be moved after what is loaded
      this.setExtent(getMaximum() - newValue - 1);
    
    }
    
    if (isValueForced) {
      // Tells the listeners about the event
      fireValueForced(this);
    }
    
    this.repaint(10);
  }

  /**
   * 
   * @return How far the buffer is loaded
   */
  public synchronized int getBufferedValue() {
    return this.bufferedSliderUI.getBufferedValue();
  }

  /**
   * @return the bufferedSliderUI
   */
  protected BufferedSliderUI getBufferedSliderUI() {
    return this.bufferedSliderUI;
  }

}
