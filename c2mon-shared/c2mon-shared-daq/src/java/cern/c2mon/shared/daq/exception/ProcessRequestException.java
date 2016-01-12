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
package cern.c2mon.shared.daq.exception;

/**
 * Exception thrown within the server if a request made to the DAQ layer is
 * unsuccessful (e.g. data tag request by the server or command).
 * 
 * @author Mark Brightwell
 *
 */
public class ProcessRequestException extends RuntimeException {


  /**
   * Serial key.
   */
  private static final long serialVersionUID = -1457578848865365203L;

  public ProcessRequestException() {
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(String message) {
    super(message);
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(Throwable cause) {
    super(cause);
    // TODO Auto-generated constructor stub
  }

  public ProcessRequestException(String message, Throwable cause) {
    super(message, cause);
    // TODO Auto-generated constructor stub
  }

}
