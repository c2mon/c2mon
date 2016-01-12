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

public class RuleSymbol  {

  public static final char SYM_NOT = '!';

  public static final char SYM_AND = '&';

  public static final char SYM_OR = '|';

  public static final char SYM_GREATER_THAN = '>';

  public static final char SYM_LESS_THAN = '<';

  public static final char SYM_EQUALS = '=';

  public static final char SYM_LEFT_PARENTHESIS = '(';

  public static final char SYM_RIGHT_PARENTHESIS = ')';

  public static final char SYM_PLUS = '+';

  public static final char SYM_MINUS = '-';

  public static final char SYM_MULTIPLY = '*';

  public static final char SYM_DIVIDE = '/';

  public static final char SYM_POWER= '^';
  

  public static boolean isSymbol(final char pSymbol) {
    switch (pSymbol) {
      case SYM_NOT:
      case SYM_LEFT_PARENTHESIS:
      case SYM_RIGHT_PARENTHESIS:
      case SYM_LESS_THAN:
      case SYM_GREATER_THAN:
      case SYM_EQUALS:
      case SYM_OR:
      case SYM_AND:
      case SYM_PLUS:
      case SYM_MINUS:
      case SYM_MULTIPLY:
      case SYM_DIVIDE:
      case SYM_POWER:
        return true;
      default:
        return false;
    }
  }
}
