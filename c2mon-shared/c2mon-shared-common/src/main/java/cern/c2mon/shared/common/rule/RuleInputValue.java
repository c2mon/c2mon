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
package cern.c2mon.shared.common.rule;

/**
 * Interface that should be implemented by any type
 * that can be fed into the rule engine.
 * 
 * <p>Needs an id, value and notion of validity.
 * 
 * @author Mark Brightwell
 *
 */
public interface RuleInputValue {

  /**
   * Returns the unique id of the input value.
   * @return the id
   */
  long getId();
  
  /**
   * Returns the value that will be fed into the rule.
   * @return the value (Integer, Float, Boolean, etc.)
   */
  Object getValue();

  /**
   * Determines whether this value is valid or not.
   * @return true if the value is valid
   */
  boolean isValid();

  

}
