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
package cern.c2mon.shared.rule.parser;

/**
 * A list of all the operators that are supported in the Rules.
 * 
 * @author ekoufaki
 */
public enum Operator {
  
  /**  Operator "+": Adding two numbers */
  ADDITION("+"),
  
  /** Operator "-": Subtraction of two numbers */
  SUBTRACTION("-"), 
  
  /** Operator "*": Multiplication of two Numbers */
  MULTIPLICATION("*"),
  
  /** Operator "/": Division of x by y */
  DIVISION("/"), 
  
  /** Operator "^": Raising x to the power of y */
  RAISE_TO_POWER("^"),
  
  /** Operator "|": Logical OR of two Boolean values ( x OR y ) */
  LOGICAL_OR("|"),
  
  /** Operator "&": Logical AND of two Boolean values ( x AND y ) */
  LOGICAL_AND("&"),
  
  /** Operator "&&": Bitwise AND of two numeric values */
  BITWISE_AND("&&"),
  
  /** Operator "||": Bitwise OR of two numeric values */
  BITWISE_OR("||"),
  
  /** Operator ">": Comparison "x GREATER THAN y" */
  GREATER_THAN_COMPARISON(">"),
  
  /** Operator "<": Comparison "x LESS THAN y" */
  LESS_THAN_COMPARISON("<"),
  
  /** Operator "<=": Comparison "(x LESS THAN y) OR (x EQUALS y)" */
  LESS_THAN_OR_EQUALS_COMPARISON("<="),
  
  /** Operator ">=": Comparison "(x GREATER THAN y) OR (x EQUALS y)" */
  GREATER_THAN_OR_EQUALS_COMPARISON(">="),
  
  /** Operator "=": Comparison "(x EQUALS y)" */
  EQUALS_COMPARISON("="),
  
  /** Operator "!=": Comparison "(x NOT EQUAL TO y)" */
  NOT_EQUALS_COMPARISON("!=");
  
  /**
   * @param text The text used inside a rule to represent an Operator 
   */
  private Operator(final String text) {
      this.text = text;
  }

  /** The text used inside a rule to represent an Operator */
  private final String text;

  @Override
  public String toString() {
      return text;
  }
  
  /**
   * @return The operator represented by the specified token, or null if no operator was found.
   * @param operator as a string token
   */
  public static Operator fromString(final String operator) {
    
    for (Operator o: Operator.values()) {
      if (o.toString().equals(operator)) {
        return o;
      }
    }
    return null;
  }
}
