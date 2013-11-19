/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
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
  Long getId();
  
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
