package cern.c2mon.shared.rule;

public class RuleFormatException extends Exception {
  
public RuleFormatException(String reason) {
    super(reason);
  }

  public RuleFormatException(String reason, Throwable t) {
    super(reason, t);
  }

}