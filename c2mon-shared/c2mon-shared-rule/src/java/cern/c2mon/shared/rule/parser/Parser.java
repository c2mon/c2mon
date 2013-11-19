/*
 * Parser
 * Version $Revision: 1.3 $
 * Data    $Date: 2007/03/07 09:48:19 $
 */
// TIM CERN. All rights reserved.
//
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D fvalentini 06/Jun/2004        First implementation
// P wbuczak    03/Aug/2004       Code review. Little corrections needed
// D fvalenti   04/Aug/2004        Corrections executed      
// P stowisek   05/Aug/2004       Code review. 
// -------------------------------------------------------------------------
package cern.c2mon.shared.rule.parser;

import cern.c2mon.shared.rule.RuleEvaluationException;

/**
 * Used to parse {@link SimpleRuleExpression}.
 * The Parser is the class that realizes the rule engine.
 * 
 * @author Francesco Valentini
 * @author ekoufaki
 */
public class Parser extends AbstractParser {
  
  /** Parser singleton */
  private static Parser parserInstance;

  /**
   * @return Parser singleton
   */
  public static Parser getInstance() {
    
    if (parserInstance == null) {
      parserInstance = new Parser();
    }
    return parserInstance;
  }
  
  public Parser() {
  }

  @Override
  public Object calculateExpr(final Object xResult, final Object yResult, final Operator op)
      throws RuleEvaluationException {

    switch (op) {

      case ADDITION:
        /*
         * Operator "+": Adding two numbers
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).doubleValue() + ((Number) yResult).doubleValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case SUBTRACTION:
        /*
         * Operator "-": Substraction of two numbers
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).doubleValue() - ((Number) yResult).doubleValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case MULTIPLICATION:
        /*
         * Operator "*": Multiplications of two Numbers
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).doubleValue() * ((Number) yResult).doubleValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case DIVISION:
        /*
         * Operator "/": Division of x by y
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).doubleValue() / ((Number) yResult).doubleValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case RAISE_TO_POWER:
        /*
         * Operator "^": Raising x to the power of y
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(Math.pow(((Number) xResult).doubleValue(), ((Number) yResult).doubleValue()));
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case LOGICAL_OR:
        /*
         * Operator "|": Logical OR of two Boolean values ( x OR y )
         */
        if (xResult instanceof Boolean && yResult instanceof Boolean) {
          return (((Boolean) xResult).booleanValue() || ((Boolean) yResult).booleanValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case LOGICAL_AND:
        /*
         * Operator "&": Logical AND of two Boolean values ( x AND y )
         */
        if (xResult instanceof Boolean && yResult instanceof Boolean) {
          return (((Boolean) xResult).booleanValue() && ((Boolean) yResult).booleanValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case BITWISE_AND:
        /*
         * Operator "&&": Bitwise AND of two numeric values
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).longValue() & ((Number) yResult).longValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case BITWISE_OR:
        /*
         * Operator "||": Bitwise OR of two numeric values
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return new Double(((Number) xResult).longValue() | ((Number) yResult).longValue());
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case GREATER_THAN_COMPARISON:
        /*
         * Operator ">": Comparison "x GREATER THAN y"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return (((Number) xResult).doubleValue() > ((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (xResult instanceof String && yResult instanceof String) {
          return (((Comparable) xResult).compareTo((Comparable) yResult) >= 0) ? Boolean.TRUE : Boolean.FALSE;
        }
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
          .append(xResult).append(" ").append(op).append(" ")
            .append(yResult).append("\".").toString());

      case LESS_THAN_COMPARISON:
        /*
         * Operator "<": Comparison "x LESS THAN y"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return (((Number) xResult).doubleValue() < ((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (xResult instanceof String && yResult instanceof String) {
          return (((Comparable) xResult).compareTo((Comparable) yResult) >= 0) ? Boolean.TRUE : Boolean.FALSE;
        } else {
          throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
            .append(xResult).append(" ").append(op).append(" ")
              .append(yResult).append("\".").toString());
        }

      case LESS_THAN_OR_EQUALS_COMPARISON:
        /*
         * Operator "<=": Comparison "(x LESS THAN y) OR (x EQUALS y)"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return (((Number) xResult).doubleValue() <= ((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (xResult instanceof String && yResult instanceof String) {
          return (((Comparable) xResult).compareTo((Comparable) yResult) >= 0) ? Boolean.TRUE : Boolean.FALSE;
        } else {
          throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
            .append(xResult).append(" ").append(op).append(" ")
              .append(yResult).append("\".").toString());
        }

      case GREATER_THAN_OR_EQUALS_COMPARISON:
        /*
         * Operator ">=": Comparison "(x GREATER THAN y) OR (x EQUALS y)"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return (((Number) xResult).doubleValue() >= ((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        }
        if (xResult instanceof String && yResult instanceof String) {
          return (((Comparable) xResult).compareTo((Comparable) yResult) >= 0) ? Boolean.TRUE : Boolean.FALSE;
        } else {
          throw new RuleEvaluationException(new StringBuffer("Error in rule definition: cannot evaluate \"")
            .append(xResult).append(" ").append(op).append(" ")
              .append(yResult).append("\".").toString());
        }

      case EQUALS_COMPARISON:
        /*
         * Operator "=": Comparison "(x EQUALS y)"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return ((Number) xResult).doubleValue() == (((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
          return xResult.equals(yResult) ? Boolean.TRUE : Boolean.FALSE;
        }

      case NOT_EQUALS_COMPARISON:
        /*
         * Operator "!=": Comparison "(x NOT EQUAL TO y)"
         */
        if (xResult instanceof Number && yResult instanceof Number) {
          return ((Number) xResult).doubleValue() != (((Number) yResult).doubleValue()) ? Boolean.TRUE : Boolean.FALSE;
        } else {
          return (!xResult.equals(yResult)) ? Boolean.TRUE : Boolean.FALSE;
        }

      default:
        throw new RuleEvaluationException(new StringBuffer("Error in rule definition: invalid operator \"")
          .append(op).append("\".").toString());
    }
  }
}
