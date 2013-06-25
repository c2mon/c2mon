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
package cern.c2mon.client.ext.history.tag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cern.c2mon.client.ext.history.common.tag.HistoryTagConfiguration;
import cern.c2mon.client.ext.history.common.tag.HistoryTagExpressionException;
import cern.c2mon.client.ext.history.common.tag.HistoryTagParameter;
import cern.tim.shared.common.type.TypeConverter;

/**
 * Interpret a history tag expression
 * 
 * @author vdeila
 */
class HistoryTagExpressionInterpreter {

  /** The pattern used for interpreting splitting the arguments */
  private static final Pattern EXPRESSION_PATTERN = Pattern.compile("\\s*([^ =]+)(?:\\s*=\\s*('[^']+'|[^ ]+)\\s*)?", Pattern.CASE_INSENSITIVE);
  
  /** The pattern for checking that the expression is acceptable, formating wise */
  private static final Pattern EXPRESSION_MATCH_PATTERN = Pattern.compile(String.format("(%s)*", EXPRESSION_PATTERN.pattern()), Pattern.CASE_INSENSITIVE);
  
  /** The expression which is interpreted */
  private final String expression;
  
  /** The history tag configuration interpreted from the {@link #expression} */
  private final HistoryTagConfiguration configuration;
  
  /**
   * @param expression
   *          an expression created with the
   *          {@link HistoryTagConfiguration#createExpression()}
   * @throws HistoryTagExpressionException
   *           if any of the arguments is invalid
   */
  public HistoryTagExpressionInterpreter(final String expression) throws HistoryTagExpressionException {
    this.expression = expression;
    this.configuration = new HistoryTagConfigurationImpl();
    evaluateExpression();
  }

  /**
   * Evaluates the {@link #expression}, updates the {@link #configuration} or
   * throws an exception trying.
   * 
   * @throws HistoryTagExpressionException
   *           if any of the arguments is invalid
   */
  private void evaluateExpression() throws HistoryTagExpressionException {
    // Splits the expression into arguments with its optional value
    if (!EXPRESSION_MATCH_PATTERN.matcher(this.expression).matches()) {
      throw new HistoryTagExpressionException(String.format("The expression '%s' is not valid.", this.expression));
    }

    final Matcher matcher = EXPRESSION_PATTERN.matcher(this.expression);
    
    while (matcher.find()) {
      if (matcher.groupCount() <= 0 || matcher.groupCount() > 2) {
        throw new HistoryTagExpressionException(String.format("Failed to interpret the expression '%s'", this.expression));
      }
      // Separates the argument from the value
      final String argument = matcher.group(1);
      String value;
      if (matcher.groupCount() == 2) {
        value = matcher.group(2);
        if (value != null && value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') {
          value = value.substring(1, value.length() - 1);
        }
      }
      else {
        value = null;
      }
      // Evaluates the argument and value.
      evaluateArgument(argument, value);
    }
  }
  
  /**
   * Evaluates an argument with its optional value, and register it in the <code>configuration</code>
   * 
   * @param argument the argument name
   * @param value the value of the argument, can be <code>null</code> if the given argument supports it
   * @throws HistoryTagExpressionException if the argument or value is invalid.
   */
  private void evaluateArgument(final String argument, final String value) throws HistoryTagExpressionException {
    HistoryTagParameter parameter = null;
    String valueToCast;
    if (value == null) {
      // The argument is an enum
      parameter = HistoryTagParameter.findByEnumValue(argument);
      valueToCast = argument;
      if (parameter == null) {
        throw new HistoryTagExpressionException(String.format("The argument '%s' without a value is not valid.", argument));
      }
    }
    else {
      // The argument is an argument with value
      parameter = HistoryTagParameter.findByArgument(argument);
      valueToCast = value;
      if (parameter == null) {
        throw new HistoryTagExpressionException(String.format("The argument '%s' is not valid!", argument));
      }
    }
    
    try {
      configuration.setValue(parameter, TypeConverter.cast(valueToCast, parameter.getType()));
    }
    catch (Exception e) {
      throw new HistoryTagExpressionException(String.format(
          "Failed to set the argument '%s' with the value '%s'",
          argument, value), e);
    }
  }
  
  /**
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }
  
  /**
   * @return the history tag configurations which is read from the expression
   */
  public HistoryTagConfiguration getHistoryTagConfiguration() {
    return this.configuration;
  }
}
