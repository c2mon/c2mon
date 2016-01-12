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
package cern.c2mon.client.ext.history.common.exception;

import cern.c2mon.client.ext.history.common.HistoryLoadingConfiguration;

/**
 * Exception which is thrown if any of the parameters in
 * {@link HistoryLoadingConfiguration} is invalid
 * 
 * @author vdeila
 * 
 */
public class LoadingParameterException extends Exception {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -564876438430752111L;

  public LoadingParameterException() { }

  public LoadingParameterException(String message) {
    super(message);
  }

  public LoadingParameterException(Throwable cause) {
    super(cause);
  }

  public LoadingParameterException(String message, Throwable cause) {
    super(message, cause);
  }

}
