package cern.c2mon.shared.rule;

public class RuleEvaluationException extends Exception {
  public RuleEvaluationException(final String pMsg) {
    super (pMsg);
  }

  public RuleEvaluationException() {
    super();
  }

  public RuleEvaluationException(String message, Throwable cause) {
    super(message, cause);
  }

  public RuleEvaluationException(Throwable cause) {
    super(cause);
  }
  
  
}