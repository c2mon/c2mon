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
package cern.c2mon.server.common.exception;

/**
 * Exception that may be raised while manipulating a SubEquipment entity. It
 * extends the Exception class
 * 
 * @author mruizgar
 * 
 */
public class SubEquipmentException extends RuntimeException {

  /**
   * Default constructor
   */
  public SubEquipmentException() {
    super();
  }

  /**
   * Constructor used when the original exception message should be propagated
   * 
   * @param msg
   *          A message that we want propagate in the exception chain
   */
  public SubEquipmentException(String msg) {
    super(msg);
  }
}
