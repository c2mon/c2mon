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
