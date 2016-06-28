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
package cern.c2mon.shared.common;

/**
 * @author J. Stowisek
 * @version $Revision: 1.2 $ ($Date: 2007/03/07 09:48:19 $ - $State: Exp $)
 */

public class ConfigurationException extends RuntimeException {
  private Exception exception;

  private int errCode = 10;

  public static final int INVALID_PARAMETER_VALUE = 0;
  public static final int INVALID_EJB_REFERENCE = 1;
  public static final int ENTITY_EXISTS = 2;  
  public static final int ENTITY_DOES_NOT_EXIST = 3;  
  public static final int UNDEFINED = 10;
  
 
  /**
   * Creates a new ConfigurationException wrapping another exception, and with a detail message.
   * @param message the detail message.
   * @param exception the wrapped exception.
   */
  public ConfigurationException(int errCode, String message, Exception exception) {
    super(message);
    this.errCode = errCode;
    this.exception = exception;
  }

  /**
   * Creates a ConfigurationException with the specified detail message.
   * @param message the detail message.
   */
  public ConfigurationException(int errCode, String message) {
    this(errCode, message, null);
  }  

  /**
   * Creates a new ConfigurationException wrapping another exception, and with no detail message.
   * @param exception the wrapped exception.
   */
  public ConfigurationException(int errCode, Exception exception) {
    this(errCode, null, exception);
  }
  
  public int getErrorCode() {
	  return this.errCode;
  }

  /**
   * Gets the wrapped exception.
   *
   * @return the wrapped exception.
   */
  public Exception getException() {
    return exception;
  }

  /**
   * Retrieves (recursively) the root cause exception.
   *
   * @return the root cause exception.
   */
  public Exception getRootCause() {
    if (exception instanceof ConfigurationException) {
      return ((ConfigurationException) exception).getRootCause();
    }
    return exception == null ? this : exception;
  }

  public String toString() {
    if (exception instanceof ConfigurationException) {
      return ((ConfigurationException) exception).toString();
    }
    return exception == null ? super.toString() : exception.toString();
  }  
}
