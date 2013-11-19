package cern.c2mon.shared.rule;

/**
 * Used to indicate whether the Rule was validated correctly or not.
 * 
 * In case the Validation has failed, then {@link #errorMessage} contains a description
 * of the error.
 *
 * @author ekoufaki
 */
public class RuleValidationReport {
  
  /** Indicates whether the Rule was validated correctly or not. */
  private final boolean isValid;
  
  /** 
   * Contains a description of the error
   * (in case the Validation has failed).
   */
  private final String errorMessage;

  public RuleValidationReport(final boolean isValid, final String errorMessage) {
    this.isValid = isValid;
    this.errorMessage = errorMessage;
  }
  
  public RuleValidationReport(final boolean isValid) {
    this.isValid = isValid;
    this.errorMessage = null;
  }
  
  /**
   * @return a description of the error
   * (in case the Validation has failed).
   */
  public String getErrorMessage() {
    return errorMessage;
  }
  
  /**
   * @return whether the Rule was validated correctly or not.
   */
  public boolean isValid() {
    return isValid;
  }
}
