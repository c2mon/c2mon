/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2010 CERN. This program is free software; you can
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
 ******************************************************************************/
package cern.c2mon.server.common.alarm;



/**
 * <b>NOTE: imported as-is into C2MON</b>
 * 
 * Implementation of the AlarmCondition interface.
 *
 * A RangeAlarmCondition is defined for alarms that are to be activated if the 
 * value of the associated DataTag is in a defined range( min <= value <= max)
 * 
 * If the parameter passed to the evaluateState() method is greater than or 
 * equal to the defined minimum value AND less than or equal to the defined 
 * maximum value, the alarm state is supposed to be FaultState.ACTIVE; If the 
 * value is outside the defined range, the alarm state is supposed to be 
 * FaultState.TERMINATE.
 * 
 * If either the minimum value or the maximum value are null, the condition is
 * checked for an open range (e.g. value >= min OR value <= max).
 *
 * RangeAlarmCondition is Serializable. A serialVersionUID has been defined
 * to ensure that no serialization problems occur after minor modifications 
 * to the class.
 *
 * @author Jan Stowisek
 * @version
 */
 public class RangeAlarmCondition extends AlarmCondition {

  /** Version number of the class used during serialization/deserialization.
   * This is to ensure that minore changes to the class do not prevent us
   * from reading back AlarmConditions we have serialized earlier. If
   * fields are added/removed from the class, the version number needs to 
   * change.
   */
  static final long serialVersionUID = -1234567L;
  
  /**
   * Lower boundary of the alarm range. May be null.
   * Please note that the maxValue MUST be of the same type as the associated
   * data tag.
   */
  protected Comparable minValue = null;

  /**
   * Upper boundary of the alarm range. May be null.
   * Please note that the maxValue MUST be of the same type as the associated
   * data tag.
   */
  protected Comparable maxValue = null;
  
  /**
   * Default Constructor 
   * This constructor should only used when creating an AlarmCondition object
   * from its XML representation.
   */
  protected RangeAlarmCondition() {
    // nothing to do
  }


  /**
   * Constructor
   * @param pMin lower limit of the alarm range (may be null)
   * @param pMax upper limit of the alarm range (may be null)
   */
  public RangeAlarmCondition(final Comparable pMin, final Comparable pMax) {
    this.minValue = pMin;
    this.maxValue = pMax;
  }
 
  /**
   * Implementation of the AlarmCondition interface
   * @param pValue the current value of the associated DataTag
   * @return ACTIVE if the value means that the alarm should be activated,
   * TERMINATE if the value means that the alarm should be terminated.
   */
  public String evaluateState(final Object pValue) {
    // If the value is null, the alarm will always be terminated
    boolean result = (pValue != null);
    
    
    // Check for the lower boundary
    if (this.minValue != null) {
        result = result && 
        this.minValue.getClass().equals(pValue.getClass()) && 
        minValue.compareTo(pValue) <= 0;
    }

    // Check for the upper boundary
    if (this.maxValue != null) {
        result = result && 
        this.maxValue.getClass().equals(pValue.getClass()) && 
        maxValue.compareTo(pValue) >= 0;
    }
    
    return result ? AlarmCondition.ACTIVE : AlarmCondition.TERMINATE;
  }
  
  /**
   * Get the lower range limit for the alarm condition.
   * This method will return null if no lower limit is defined
   * @return the lower range limit for the alarm condition.
   */
  public Comparable getMinimumValue () {
    return this.minValue;
  }
  
  /**
   * Get the upper range limit for the alarm condition.
   * This method will return null if no upper limit is defined
   * @return the upper range limit for the alarm condition.
   */
  public Comparable getMaximumValue () {
    return this.maxValue;
  }

  public Object clone() {
    return new RangeAlarmCondition(this.minValue, this.maxValue);
  }
  
  /**
   * @return a String representation of the object
   */
  public String toString() {
    StringBuffer str = new StringBuffer ("ACTIVE if the tag value is");
    if (this.minValue != null) {
      str.append(" >= ");
      str.append(this.minValue);
      if (this.maxValue != null) {
        str.append(" and ");
      }
      else {
        str.append(".");
      }
    }
    if (this.maxValue != null) {
      str.append(" <= ");
      str.append(this.maxValue);
      str.append(".");
    }
    return str.toString();
  }

	public boolean equals(final Object obj) {
		boolean result = true;
		
    	if (obj instanceof RangeAlarmCondition) {
    		RangeAlarmCondition cond = (RangeAlarmCondition) obj;
    		
			if (this.minValue == null) {
				result = result && (cond.minValue == null);			
    	    }
			else {
				result = result && this.minValue.equals(cond.minValue);
			}
    	
    	    if (this.maxValue == null) {
    	    	result = result && (cond.maxValue == null);
    	    }
    	    else {
    	    	result = result && this.maxValue.equals(cond.maxValue);
    	    }
		}
    	else {
    		result = false;
    	}
    	return result;

	}
  
}
