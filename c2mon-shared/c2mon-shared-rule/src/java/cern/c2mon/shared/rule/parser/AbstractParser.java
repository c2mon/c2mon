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
 * Contains the main functions of the Rule Engine parser.
 * 
 * @see Parser
 * @see InvalidExpressionParser
 * 
 * @author Francesco Valentini
 * @author ekoufaki
 */
public abstract class AbstractParser {
  
  public AbstractParser() {
  }

  /**
   * @return The result of the expression (x) op (y)
   * 
   *         Where
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @param x Argument 1
   * @param y Argument 2
   * 
   * @throws RuleEvaluationException In case of error during the calculations.
   */
  public abstract Object calculateExpr(final Object x, final Object y, final Operator op)
      throws RuleEvaluationException;

  /**
   * @return Extracts the "right" part of the binary expression x OPERATOR y
   */
  private Object[] extractExpressionFromTheRight(final Object[] token, final int operatorIndex) {

    /**
     * (This is easier than {@link #extractExpressionFromTheLeft(Object[])} since
     * we have already identified where the OPERATOR is (@param operatorIndex),
     * so we just return whatever is left from the position operatorIndex until the end).
     */
    int dim = token.length - operatorIndex - 1;
    Object[] y = new Object[dim];
    for (int i = 0; i != dim; i++) {
      y[i] = token[i + operatorIndex + 1];
    }
    //System.out.println("-->   y   <-- " + arrayPrint(y));

    return y;
  }
  
  /**
   * @return Extracts the "left" part of the binary expression x OPERATOR y
   * 
   * This method extracts the most left parameter without the most external parents 
   * from an expression in token format. The result will be a
   * string array containing all the tokens of the parameter extracted.
   * 
   * @param token rule in tokens format.
   */
  public final Object[] extractExpressionFromTheLeft(final Object[] token) {
    Object[] result, result2; // Used to compose and return the expression
    int openParent = 0; // It counts the number of parents opened
    int index = 0;
    boolean flag;

    // Create a buffer for building the result
    result = new Object[token.length]; // max possible length for the result
    // Treat unary operators "!" and "-"
    flag = true;
    if (token[0] instanceof String) {
      if (((String) token[0]).equals("!") || ((String) token[0]).equals("-")) {
        flag = false;
        if (token[1] instanceof String && ((String) token[1]).equals("(")) {
          openParent++;
        }
      } else {
        flag = true;
      }
    }

    // If flag=FALSE the most external parents
    // are not cuted.
    // examination of tokens until to reach a valid parameter to return
    String strToken = null;
    int i = 0;
    while (i < token.length && (flag || (openParent > 0))) {
      if (token[i] instanceof String) {
        strToken = ((String) token[i]);
        if (strToken.equals("(") && (openParent == 0) && flag) {
          openParent++;
          i++;
        } else if (strToken.equals(")") && (openParent == 1) && flag) {
          openParent--;
          break; // This means that there is an parameter in result and we are on the last left parent
        } else {
          result[index] = strToken; // It add the current token at the result
          index++;
          if (strToken.compareTo("(") == 0) {
            openParent++;
          } else if (strToken.compareTo(")") == 0) {
            openParent--; // It take count about the open parents
          } else if ((openParent == 0) && (strToken.compareTo("!") != 0) && (strToken.compareTo("-") != 0)) {
            break; // When the last parent is closed the loop ends.
          }
          i++;
        }
      } else {
        result[index] = token[i]; // It add the current token at the result
        index++;
        i++;
        if (openParent == 0) {
          break;
        }
      }
    }
    /*
     * Create a new array of the correct length that only contains the tokens that are part of the result.
     */
    result2 = new Object[index];
    for (i = 0; i != index; i++) {
      result2[i] = result[i];
    }
    /*
     * Return this result array.
     */
    return result2;
  }

  /**
   * @return Whether the Parenthesis in the given rule are balanced or not
   * 
   * @param token rule in tokens format.
   */
  public final boolean isParenthesisBalanced(final Object[] token) {
    int nesting = 0;
    for (Object c : token) {

      if (c instanceof Character || c instanceof String) {
        char ch = 0;
        if (c instanceof Character) {
          ch = ((Character) (c)).charValue();
        }
        if (c instanceof String) {
          ch = ((String) (c)).charAt(0);
        }

        switch (ch) {
          case '(':
            nesting++;
            break;
          case ')':
            nesting--;
            if (nesting < 0) {
              return false;
            }
            break;
        }
      }
    }
    return nesting == 0;
  }

  /**
   * @return TRUE if the rule given in token format is always true.
   * 
   * @param token rule in tokens format.
   */
  public final boolean isAlwaysTrue(final Object[] token) {

    // there must be only one token in the rule
    if (token.length != 1) {
      return false;
    }

    final Object result = token[0];

    // check if THE ONLY token of the rule, is actually the "TRUE" token
    if (((Boolean) result).equals(Boolean.TRUE)) {
      return true;
    }
    return false;
  }

  /**
   * eval() method 
   * 
   * @return
   * This is a recursive procedure that computes the formula in token format and returns the result 
   * as a generic Object that should be down casted in a specific Double(),String(), or Boolean() 
   * java object. 
   * 
   * The procedure for each recursive called reduce of some elements the initial token array with the
   * formula to compute.
   * 
   * @param token rule in tokens format.
   */
  public final Object eval(final Object[] token) throws RuleEvaluationException {
    //    System.out.println("--> token <-- " + arrayPrint(token));
    Object[] x, y; // Contain the 2 main parameter of the formula
    String op; // Contains the main operator

    // Let's make sure the expression is using parenthesis in the right way
    // (the error would be caught later on anyway, however this way we return a more specific
    // error message to the user)
    if (!isParenthesisBalanced(token)) {
      throw new RuleEvaluationException(new StringBuffer("Parenthesis not balanced!").toString());
    }

    /*
     * CASE 1: If there is only one token in the rule, return the token itself as the rule result. 
     * This should only happen if the token is a Number, a String or
     * a Boolean value.
     */
    if (token.length == 1) {
      return token[0];
    }

    // ----------------------- RECURSIVE CONTROLS -------------------------
    try {
      
      /*
       * Extract the leftmost parameter of the formula
       */
      x = extractExpressionFromTheLeft(token);
      //      System.out.println("-->   x   <-- " + arrayPrint(x));
      
      /*
       * Special case: "(" expression ")" Just remove the parentheses and evaluate the remaining expression.
       */
      if (token[0] instanceof String 
          && token[token.length - 1] instanceof String 
          && (x.length) == (token.length - 2) 
          && token[0].equals("(")
          && token[token.length - 1].equals(")")) {
        
        return eval(x);
      }
      /*
       * Special case: unary operators "!" and "-"
       */
      if (x.length == token.length) {
        Object[] x2 = new Object[x.length - 1];
        for (int i = 1; i < x.length; i++) {
          x2[i - 1] = x[i];
        }
        if (x[0] instanceof String && x[0].equals("!")) {
          return eval(x2).equals(Boolean.TRUE) ? Boolean.FALSE : Boolean.TRUE;
        } else if (x[0] instanceof String && x[0].equals("-")) {
          return new Double(-((Number) eval(x2)).doubleValue());
        } else {
          throw new RuleEvaluationException(new StringBuffer("Error in rule: cannot handle unary operator ")
          .append(x[0]).append(".").toString());
        }
      }

      final int operatorIndex = getIndexOfMainOperator(token, x);
      op = (String) token[operatorIndex]; // op contains the main operator of the formula in token[].
      //      System.out.println("-->   op  <-- " + op);

      y = extractExpressionFromTheRight(token, operatorIndex);

      /*
       * Recursively calculate the result of the binary expression x OPERATOR y depending on the operator.
       */
      Object xResult = eval(x);
      Object yResult = eval(y);

      //      System.out.println("------ ");
      final Object result = calculateExpr(xResult, yResult, Operator.fromString(op));
      return result;

    } catch (ClassCastException cce) {
      throw cce;
    } catch (Exception e) {
      throw new RuntimeException("Unexpected error during rule evaluation.", e);
    }
  }

  /**
   * @return Find the main operator in the binary expression x OPERATOR y.
   */
  private int getIndexOfMainOperator(final Object[] token, final Object[] x) {

    final int operatorIndex = (token[0] instanceof String && (((String) token[0]).compareTo("(") == 0))  
        // the position of the main operator
        ? (x.length + 2)  : (x.length);

    return operatorIndex;
  }


  public String arrayPrint(final Object[] params) {
    StringBuffer str = new StringBuffer();
    //    str.append(params.length);
    str.append("[");
    for (int i = 0; i < params.length; i++) {
      str.append(params[i]);
      if (i < (params.length - 1)) {
        str.append(", ");
      }
    }
    str.append("]");
    return str.toString();
  }
}
