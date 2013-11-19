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