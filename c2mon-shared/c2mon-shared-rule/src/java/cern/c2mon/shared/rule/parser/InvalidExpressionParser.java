
package cern.c2mon.shared.rule.parser;

import cern.c2mon.shared.rule.RuleEvaluationException;

/**
 * Used to parse expressions that contain INVALID tags.
 * 
 * Same as the normal {@link Parser}
 * but the arguments of the Expression can also be INVALID.
 * @see https://issues.cern.ch/browse/TIM-823
 * 
 * Please note there is 2 KIND of INVALID constants {@link RuleConstant}:
 * 
 * - {@link RuleConstant#INTERNAL_INVALID} (used internally from the Parser to evaluate 
 * expressions with invalid tags)
 * 
 * - {@link RuleConstant#INVALID_KEYWORD} (a keyword to
 * construct rules that check against a Tag's quality)
 * 
 * @see https://issues.cern.ch/browse/TIM-835
 * 
 * @author ekoufaki
 */
public class InvalidExpressionParser extends Parser {
  
  /** Parser singleton */
  private static InvalidExpressionParser parserInstance;

  /**
   * @return Parser singleton
   */
  public static InvalidExpressionParser getInstance() {
    
    if (parserInstance == null) {
      parserInstance = new InvalidExpressionParser();
    }
    return parserInstance;
  }
  
  /**
   * @return The result of the expression (x) op (y)
   * (if the Expression that contains INVALID tags is possible to be evaluated),
   * INVALID otherwise.
   * 
   * This is basically the same as {@link Parser#calculateExpr(Object, Object, Operator)} 
   * but (x), (y) can also be one of the following: 
   * {@link RuleConstant#INTERNAL_INVALID}, 
   * {@link RuleConstant#INVALID_KEYWORD}.
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @param x Argument 1
   * @param y Argument 2
   * 
   * @throws RuleEvaluationException In case of error during the calculations.
   */
  @Override
  public final Object calculateExpr(final Object x, final Object y, final Operator op)
      throws RuleEvaluationException {
    
    // Let's add a special case for rules with INVALID Tags
    if (isInvalid(x)
        || isInvalid(y)) {

      return calculateExprWithInvalids(x, y, op);
    }
    // if the expression DOES NOT contain INVALID Tags =>
    return super.calculateExpr(x, y, op); // just call the normal parser
  }
  
  /**
   * @return The result of the expression (x) op (y)
   * (if the Expression that contains INVALID tags is possible to be evaluated),
   * INVALID otherwise.
   * 
   * This is basically the same as {@link Parser#calculateExpr(Object, Object, Operator)} 
   * but (x), (y) can also be one of the following: 
   * {@link RuleConstant#INTERNAL_INVALID}, 
   * {@link RuleConstant#INVALID_KEYWORD}.
   * 
   * DEPENDING on the whether ONE or BOTH of the arguments are:
   * {@link RuleConstant#INTERNAL_INVALID}, or 
   * {@link RuleConstant#INVALID_KEYWORD},
   * the expression is calculated in a different way.
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @param x Argument 1
   * @param y Argument 2
   * 
   * @throws RuleEvaluationException In case of error during the calculations.
   */
  public final Object calculateExprWithInvalids(final Object x, final Object y, final Operator op) 
      throws RuleEvaluationException {
    
    final boolean containsInvalidKeyword = isInvalidKeyword(x) || isInvalidKeyword(y);
    final boolean containsInternalInvalid = isInternalInvalidToken(x) || isInternalInvalidToken(y);
    
    if (containsInvalidKeyword && containsInternalInvalid) {
      return calculateExprWithInvalids(op);
    }
    else if (containsInvalidKeyword) {
      return calculateExprWithInvalidKeyword(x, y, op);
    }
    else if (containsInternalInvalid) {
      return calculateExprWithInternalInvalid(x, y, op);
    }
    
    throw new RuleEvaluationException();
  }
  
  /**
   * @return The result of the expression (x) op (y)
   * 
   * Where:
   * ArguementX is the {@link RuleConstant#INVALID_KEYWORD}
   * ArguementY is the {@link RuleConstant#INTERNAL_INVALID}
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @throws RuleEvaluationException In case of error during the calculations.
   * (For example the operator (>) does not make sense with the arguments above)
   */
  public final Object calculateExprWithInvalids(final Operator op) 
      throws RuleEvaluationException {
    
    switch (op) {
      
      case EQUALS_COMPARISON:
        return true;
        
      case NOT_EQUALS_COMPARISON:
        return false;

      default:
        throw new RuleEvaluationException();
    }    
  }


  /**
   * @return The result of the expression (x) op (y)
   * 
   * ONLY for the case where EITHER X or Y is the {@link RuleConstant#INVALID_KEYWORD}
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @param x Argument 1
   * @param y Argument 2
   * 
   * @throws RuleEvaluationException In case of error during the calculations.
   * (For example the operator (>) does not make sense 
   * when one of the arguments is the  {@link RuleConstant#INVALID_KEYWORD})
   */
  public final Object calculateExprWithInvalidKeyword(final Object x, final Object y, final Operator op) 
      throws RuleEvaluationException {
    
    switch (op) {
      
      case EQUALS_COMPARISON:
        
        if (x instanceof String && y instanceof String) {
          
          if (RuleConstant.fromString((String) x)
              .equals(RuleConstant.fromString((String) y))) {
            return true;
          }
        }
        return false;
        
      case NOT_EQUALS_COMPARISON:
        
        if (x instanceof String && y instanceof String) {
          
          if (RuleConstant.fromString((String) x)
              .equals(RuleConstant.fromString((String) y))) {
            return false;
          }
        }
        return true;

      default:
        throw new RuleEvaluationException("The expression: " + x + " " + op + " " + y 
            + " does not make sense!");
    }    
  }
  
  
  /**
   * @return the Result of the expression (x) op (y),
   * (if the Expression that contains INVALID tags is possible to be evaluated),
   * INVALID otherwise.
   * 
   * IMPORTANT: It only makes sense to call this method
   * if AT LEAST ONE of the parameters: (x), (y) IS Invalid.
   * 
   * @param op Any of the following: +, -, *, /, ||, |, &&, &, as defined in {@link Operator}
   * 
   * @param x Argument 1
   * @param y Argument 2
   */
  public final Object calculateExprWithInternalInvalid(final Object x, final Object y, final Operator op) {

      switch (op) {
        
        case LOGICAL_OR:
          final Boolean bOr = x instanceof Boolean ?
              ((Boolean) x).booleanValue() : ((Boolean) y).booleanValue();

          // TRUE   || INVALID == TRUE
          // FALSE  || INVALID == INVALID
          if (bOr) {
            return true;
          }
          return RuleConstant.INTERNAL_INVALID.toString();
          
        case LOGICAL_AND:
          final Boolean bAnd = x instanceof Boolean ?
              ((Boolean) x).booleanValue() : ((Boolean) y).booleanValue();
              
          // TRUE   && INVALID == INVALID
          // FALSE  && INVALID == FALSE
          if (!bAnd) {
            return false;
          }
          return RuleConstant.INTERNAL_INVALID.toString();
          
        default:
          return RuleConstant.INTERNAL_INVALID.toString();
      }
    }
  

  
  /**
   * @return True, if the specified token is the RuleConstant.INVALID (keyword)
   * OR the RuleConstant.INTERNAL_INVALID.
   * 
   * @param token The token to be checked.
   */
  private static boolean isInvalid(final Object token) {
    
    if (token instanceof String) {
      final RuleConstant r = RuleConstant.fromString((String) token);
      if (r != null) {
        if (r.equals(RuleConstant.INVALID_KEYWORD)
            || r.equals(RuleConstant.INTERNAL_INVALID)
            ) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * @return True, if the specified token is the RuleConstant.INTERNAL_INVALID.
   * 
   * @param token The token to be checked.
   */
  private static boolean isInternalInvalidToken(final Object token) {
    
    if (token instanceof String) {
      final RuleConstant r = RuleConstant.fromString((String) token);
      if (r != null) {
        if (r.equals(RuleConstant.INTERNAL_INVALID)) {
          return true;
        }
      }
    }
    return false;
  }
  
  /**
   * @return True, if the specified token is the RuleConstant.INVALID (keyword).
   * 
   * @param token The token to be checked.
   */
  private static boolean isInvalidKeyword(final Object token) {
    
    if (token instanceof String) {
      final RuleConstant r = RuleConstant.fromString((String) token);
      if (r != null) {
        if (r.equals(RuleConstant.INVALID_KEYWORD)) {
          return true;
        }
      }
    }
    return false;
  }
}
