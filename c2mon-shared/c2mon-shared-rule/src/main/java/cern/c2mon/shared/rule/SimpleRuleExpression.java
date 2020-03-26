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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cern.c2mon.shared.common.rule.RuleInputValue;
import cern.c2mon.shared.rule.parser.InvalidExpressionParser;
import cern.c2mon.shared.rule.parser.Parser;
import cern.c2mon.shared.rule.parser.RuleConstant;

public class SimpleRuleExpression extends RuleExpression implements Cloneable {

    private static final long serialVersionUID = 93178070585316435L;

    /**
     * Tokenized representation of the rule expression.
     */
    private Object[] tokens = null;

    public SimpleRuleExpression(final String pExpression) throws RuleFormatException {
        super(pExpression, RuleType.Simple);
        this.tokens = tokenize(pExpression);
    }

    public Object clone() {
        SimpleRuleExpression simpleRuleExpression = (SimpleRuleExpression) super.clone();
        return simpleRuleExpression;
    }

    public String toString() {
      
      StringBuffer str = new StringBuffer();
      for (int i = 0; i != this.tokens.length; i++) {
        
        str.append("\"");
        str.append(tokens[i]);
        str.append("\"");
              
        if (i != (tokens.length - 1)) {
              str.append(" ");
        }
      }
      return str.toString();
    }
    
    public String toXml() {

      StringBuffer str = new StringBuffer();
      str.append("<RuleExpression type=\"SimpleRuleExpression\">");
      for (int i = 0; i != this.tokens.length; i++) {
          if (tokens[i] instanceof Double) {
              str.append("Double(").append(tokens[i]).append(")");
          } else if (tokens[i] instanceof Boolean) {
              str.append("Boolean(").append(tokens[i]).append(")");
          } else {
              str.append("\"");
              str.append(tokens[i]);
              str.append("\"");
          }
          if (i != (tokens.length - 1)) {
              str.append(" ");
          }
      }
      str.append("</RuleExpression>\n");
      return str.toString();
    }

    /**
     * Extract all "tokens" (terminal symbols) from a rule expression.
     */
    protected static Object[] tokenize(String pExpression) throws RuleFormatException {
        char[] chars = pExpression.toCharArray();
        char currentChar;
        StringBuffer tempStr = null;
        // Compute the length of the rule expression
        int len = chars.length;
        // Create a buffer for storing the list of tokens
        ArrayList buffer = new ArrayList(len);
        // Iterate over formula and cut it into tokens, stored in a buffer
        int i = 0;
        while (i < len) {
            currentChar = chars[i];
            switch (currentChar) {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case '\t':
                i++;
                break;
            case '+':
                buffer.add("+");
                i++;
                break;
            case '-':
                buffer.add("-");
                i++;
                break;
            case '*':
                buffer.add("*");
                i++;
                break;
            case '/':
                buffer.add("/");
                i++;
                break;
            case '^':
                buffer.add("^");
                i++;
                break;
            case '(':
                buffer.add("(");
                i++;
                break;
            case ')':
                buffer.add(")");
                i++;
                break;
            case '=':
                buffer.add("=");
                i++;
                break;
            case '<':
                i++;
                if (i != len && chars[i] == '=') {
                    buffer.add("<=");
                    i++;
                } else {
                    buffer.add("<");
                }
                break;
            case '>':
                i++;
                if (i != len && chars[i] == '=') {
                    buffer.add(">=");
                    i++;
                } else {
                    buffer.add(">");
                }
                break;
            case '!':
                i++;
                if (i != len && chars[i] == '=') {
                    buffer.add("!=");
                    i++;
                } else {
                    buffer.add("!");
                }
                break;
            case '&':
                i++;
                if (i != len && chars[i] == '&') {
                    buffer.add("&&");
                    i++;
                } else {
                    buffer.add("&");
                }
                break;
            case '|':
                i++;
                if (i != len && chars[i] == '|') {
                    buffer.add("||");
                    i++;
                } else {
                    buffer.add("|");
                }
                break;
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                tempStr = new StringBuffer();
                tempStr.append(currentChar);
                i++;
                while (i < len && Character.isDigit(chars[i])) {
                    tempStr.append(chars[i]);
                    i++;
                }
                if (i < len && chars[i] == '.') {
                    tempStr.append('.');
                    i++;
                    while (i < len && Character.isDigit(chars[i])) {
                        tempStr.append(chars[i]);
                        i++;
                    }
                }
                if (i < len && "E".equalsIgnoreCase(chars[i]+"")) {
                    tempStr.append('E');
                    i++;
                    while (i < len && ( Character.isDigit(chars[i]) || chars[i] == '-')) {
                        tempStr.append(chars[i]);
                        i++;
                    }
                }
                buffer.add(Double.valueOf(tempStr.toString()));
                break;
            case '.':
                tempStr = new StringBuffer();
                tempStr.append(currentChar);
                i++;
                while (i < len && Character.isDigit(chars[i])) {
                    tempStr.append(chars[i]);
                    i++;
                }
                buffer.add(Double.valueOf(tempStr.toString()));
                break;
            case '$':
                tempStr = new StringBuffer("$");
                i++;
                while (i < len && Character.isLetter(chars[i])) {
                    tempStr.append(chars[i]);
                    i++;
                }
                String invalidStr = tempStr.toString();
                if (invalidStr.equalsIgnoreCase(RuleConstant.INVALID_KEYWORD.toString())) {
                    buffer.add(RuleConstant.INVALID_KEYWORD.toString());
                }
                break;    
            case '#':
                tempStr = new StringBuffer();
                i++;
                while (i < len && Character.isDigit(chars[i])) {
                    tempStr.append(chars[i]);
                    i++;
                }
                buffer.add(new RuleInputTagId(tempStr.toString()));
                break;
            case 't':
            case 'f':
                tempStr = new StringBuffer();
                tempStr.append(currentChar);
                i++;
                while (i < len && Character.isLetter(chars[i])) {
                    tempStr.append(chars[i]);
                    i++;
                }
                String str = tempStr.toString();
                if (str.equalsIgnoreCase("true")) {
                    buffer.add(Boolean.TRUE);
                } else if (str.equalsIgnoreCase("false")) {
                    buffer.add(Boolean.FALSE);
                } else {
                    throw new RuleFormatException("Unknown symbol in rule expression: " + str);
                }
                break;
            case '\"':
                tempStr = new StringBuffer();
                i++;
                while (i < len && chars[i] != '\"') {
                    tempStr.append(chars[i]);
                    i++;
                }
                if (i == len) {
                    throw new RuleFormatException("Unterminated String (" + tempStr + " ) in expression : "
                            + pExpression);
                } else {
                    buffer.add(tempStr.toString());
                    i++;
                }
                break;
            default:
                i++;
            }
        }

        // Transform the buffer into an Object array containing all the tokens
        // in the right order.
        return buffer.toArray(new Object[0]);
    }
    

    /**
     * @return TRUE if the expression is always true.
     * 
     * (this is useful to detect unreachable statements)
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     */
    public final boolean isAlwaysTrue(final Map<Long, Object> pInputParams) throws RuleEvaluationException {

        final Object[] valueTokens = splitToTokens(pInputParams);
        return Parser.getInstance().isAlwaysTrue(valueTokens);
    }
    
    /**
     * @return True if any of the Datatags contained in the rule is Invalid, false otherwise.
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     */
    public final boolean hasInvalidTags(final Map<Long, Object> pInputParams) {

      RuleInputValue tag = null;
      
      for (int i = 0; i < tokens.length; i++) {
          if (tokens[i] instanceof RuleInputTagId) {

            final RuleInputTagId tagInput = (RuleInputTagId) tokens[i];
            final long tagId = tagInput.getId();
            final Object val = pInputParams.get(tagId);
            
            if (val != null && val instanceof RuleInputValue) {
              tag = (RuleInputValue) val;
              if (!tag.isValid()) {
                return true;
              }
            }
          }
      }
      return false;
    }
    
    /**
     * @return True if the {@link SimpleRuleExpression} 
     * contains the {@link RuleConstant#INVALID}.
     */
    public final boolean usesTheInvalidKeyword() {

      for (int i = 0; i < tokens.length; i++) {
        if (tokens[i].equals(RuleConstant.INVALID_KEYWORD.toString())) {
          return true;
        }
      }
      return false;
    }
    
    
    
    /**
     * Same as {@link #splitToTokens(Map)} but Invalid tags are allowed.
     * The result can be passed to the {@link InvalidExpressionParser}
     * so that the expression can be calculated.
     * 
     * @return The rule in token format
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     * 
     * @throws RuleEvaluationException in case the DataTags contained in the Rule
     * are Null, or non-existent.
     */
    private Object[] splitToTokensAndAllowInvalidTags(final Map<Long, Object> pInputParams) 
        throws RuleEvaluationException {
      
      Object[] valueTokens = new Object[tokens.length];
      RuleInputValue tag = null;
      for (int i = 0; i < tokens.length; i++) {
          if (tokens[i] instanceof RuleInputTagId) {
            
              RuleInputTagId tagInput = (RuleInputTagId) tokens[i];
              long tagId = tagInput.getId();
              Object val = pInputParams.get(tagId);
              if (val != null && val instanceof RuleInputValue) {
                  tag = (RuleInputValue) val;
                  if (!tag.isValid()) {
                    valueTokens[i] = RuleConstant.INTERNAL_INVALID.toString();
                  }
                  else if (tag.getValue() == null) {
                      throw new RuleEvaluationException("Cannot evaluate rule: tag " + ((RuleInputValue) val).getId()
                              + " is null.");
                  } else {
                      valueTokens[i] = ((RuleInputValue) val).getValue();
                  }

              } else if (val != null) {
                  valueTokens[i] = val;
              } else {
                  throw new RuleEvaluationException("Cannot evaluate rule: input tag missing "
                          + ((RuleInputTagId) tokens[i]).getId());
              }
          } else {
              valueTokens[i] = tokens[i];
          }
      }
      return valueTokens;
    }
    
    /**
     * Same as {@link #splitToTokens(Map)} but Invalid tags are replaced with their values
     * and the Invalid Status is ignored.
     * 
     * @return The rule in token format
     * 
     * @see RuleExpression#forceEvaluate(Map)
     * @see http://issues/browse/TIMS-835
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     */
    private Object[] splitToTokensAndReplaceInvalidTags(final Map<Long, Object> pInputParams) {
        
      Object[] valueTokens = new Object[tokens.length];
      RuleInputValue tag = null;
      for (int i = 0; i < tokens.length; i++) {
          if (tokens[i] instanceof RuleInputTagId) {
            
              RuleInputTagId tagInput = (RuleInputTagId) tokens[i];
              long tagId = tagInput.getId();
              Object val = pInputParams.get(tagId);
              if (val != null && val instanceof RuleInputValue) {
                  tag = (RuleInputValue) val;
                  if (tag.getValue() == null) {
                      valueTokens[i] = RuleConstant.INTERNAL_INVALID.toString();
                  } else {
                      valueTokens[i] = ((RuleInputValue) val).getValue();
                  }
              } else if (val != null) {
                  valueTokens[i] = val;
              } else {
                valueTokens[i] = RuleConstant.INTERNAL_INVALID.toString();
              }
          } else {
              valueTokens[i] = tokens[i];
          }
      }
      return valueTokens;
    }
    
    /**
     * @return The rule in token format
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     * 
     * @throws RuleEvaluationException in case the DataTags contained in the Rule
     * are Invalid, Null, or non-existent.
     */
    private Object[] splitToTokens(final Map<Long, Object> pInputParams) 
        throws RuleEvaluationException {
      
      Object[] valueTokens = new Object[tokens.length];
      RuleInputValue tag = null;
      for (int i = 0; i < tokens.length; i++) {
          if (tokens[i] instanceof RuleInputTagId) {
            
              RuleInputTagId tagInput = (RuleInputTagId) tokens[i];
              long tagId = tagInput.getId();
              Object val = pInputParams.get(tagId);
              if (val != null && val instanceof RuleInputValue) {
                  tag = (RuleInputValue) val;
                  if (tag.getValue() == null) {
                      throw new RuleEvaluationException("Cannot evaluate rule: tag " 
                          + ((RuleInputValue) val).getId()
                              + " is null.");
                  } else {
                      valueTokens[i] = ((RuleInputValue) val).getValue();
                  }

              } else if (val != null) {
                  valueTokens[i] = val;
              } else {
                  throw new RuleEvaluationException("Cannot evaluate rule: input tag missing "
                          + ((RuleInputTagId) tokens[i]).getId());
              }
          } else {
              valueTokens[i] = tokens[i];
          }
      }
      return valueTokens;
    }
    
    /**
     * @return True if the result of the evaluation is INVALID.
     * (As a convention the RuleParser returns 
     * the {@link RuleConstant#INTERNAL_INVALID} in such a case)
     * 
     * @param result The result after the evaluation of a rule.
     */
    private boolean isResultInvalid(final Object result) {

      if (result instanceof String) {
        final String r = (String) result;
        if (r.equals(RuleConstant.INTERNAL_INVALID.toString())) {
          return true;
        }
      }
      return false;
    }
    
    /**
     * RuleExpressions that contain INVALID tags use a different Parser for the evaluation.
     * @return The result of the expression (if possible), RuleEvaluationException otherwise.
     * 
     * @param pInputParams Map of value objects related to the input tag ids
     * @throws RuleEvaluationException
     */
    private Object handleRuleWithInvalidTags(final Map<Long, Object> pInputParams)
        throws RuleEvaluationException {

      final Object result = tryToIgnoreInvalidTags(pInputParams);
      if (isResultInvalid(result)) {
        throw new RuleEvaluationException("Cannot evaluate rule: Invalid tags found!");
      }
      return result;
    }
    
    /**
     * RuleExpressions that contain no INVALID tags use a different Parser for the evaluation.
     * @return The result of the expression (if possible), RuleEvaluationException otherwise.
     * @param pInputParams Map of value objects related to the input tag ids
     * @throws RuleEvaluationException
     */
    private Object handleRuleWithNoInvalidTags(final Map<Long, Object> pInputParams)
        throws RuleEvaluationException {

      final Object[] valueTokens = splitToTokens(pInputParams);
      final Object result = Parser.getInstance().eval(valueTokens); // => evaluate the expression as normal
      return result;
    }
    
    @Override
    public final Object evaluate(final Map<Long, Object> pInputParams) throws RuleEvaluationException {

      if (hasInvalidTags(pInputParams) || usesTheInvalidKeyword()) {
        // invalid tags found!  =>
        return handleRuleWithInvalidTags(pInputParams);
      }
      // no invalid tags in the expression =>
      return handleRuleWithNoInvalidTags(pInputParams);
    }
    
    @Override
    public final Object forceEvaluate(final Map<Long, Object> pInputParams)  {
      try {
        return handleRuleWithNoInvalidTags(pInputParams);
      } catch (Exception e) {
        return null;
      }
    }
    
    
    /**
     * Used to calculate expressions that contain INVALID tags (if possible).
     * 
     * @return The result of the expression (if possible), RuleConstant.INVALID otherwise.
     * @throws RuleEvaluationException In case the Expression could not be evaluated.
     */
    private Object tryToIgnoreInvalidTags(final Map<Long, Object> pInputParams) 
        throws RuleEvaluationException {
      
      final Object[] valueTokens = splitToTokensAndAllowInvalidTags(pInputParams);
      final Object result = InvalidExpressionParser.getInstance().eval(valueTokens);
      
      return result;
    }
    
    @Override
    public Set<Long> getInputTagIds() {
        Set<Long> ids = new LinkedHashSet<Long>();
        for (int i = 0; i < this.tokens.length; i++) {
            if (tokens[i] instanceof RuleInputTagId) {
                ids.add(((RuleInputTagId) tokens[i]).getId());
            }
        }
        return ids;
    }

    public static void testExpression(final String pExpression) {
        System.out.println("Rule to evaluate: " + pExpression);
        try {
            SimpleRuleExpression exp = new SimpleRuleExpression(pExpression);
            Object result = exp.evaluate(new Hashtable());
            System.out.println("--> result type:  " + result.getClass().getName());
            System.out.println("--> result value: " + result.toString());
            System.out.println(exp.toString());
        } catch (Exception e) {
            System.out.println("--> error: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    @Override
    public RuleValidationReport validate(final Map<Long, Object> pInputParams) {
      
      try {       
        final Object[] valueTokens = splitToTokensAndAllowInvalidTags(pInputParams);
        InvalidExpressionParser.getInstance().eval(valueTokens);
      } 
      catch (Exception e) {
        return new RuleValidationReport(false, e.getMessage());
      }
      return new RuleValidationReport(true);
    }
}
