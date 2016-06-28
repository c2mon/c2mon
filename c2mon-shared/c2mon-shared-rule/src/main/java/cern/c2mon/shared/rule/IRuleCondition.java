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
package cern.c2mon.shared.rule;

import java.util.Set;

/**
 * Interface that describes the API of a RuleCondition
 *
 * @author Matthias Braeger
 */
public interface IRuleCondition extends IRuleExpression {

  /**
   * This method returns the result value which should be applied, in case that
   * the rule expression is met
   * @return The result value that should be returned, in case the expression is <code>true</code>
   */
  Object getResultValue();
  
  /**
   * @return The list of tag ids which are used within the conditioned rule
   */
  Set<Long> getInputTagIds();
  
  /**
   * @return The rule expression as String
   */
  String getExpression();
}
