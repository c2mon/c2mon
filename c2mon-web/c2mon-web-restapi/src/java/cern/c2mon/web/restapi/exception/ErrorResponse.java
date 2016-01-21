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
package cern.c2mon.web.restapi.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * This class is used to hold a HTTP status and an error message which will be
 * serialised and returned to clients when errors occur during API requests.
 *
 * @author Justin Lewis Salmon
 */
@Data
class ErrorResponse {

  /**
   * The HTTP error status code (404, 500, etc.)
   */
  int status;

  /**
   * The message describing the reason for the error.
   */
  String message;

  /**
   * Constructor.
   *
   * @param status the {@link HttpStatus} of the error
   * @param throwable the exception that was thrown
   */
  public ErrorResponse(final HttpStatus status, final Throwable throwable) {
    this.status = status.value();
    this.message = throwable.getMessage();
  }

  /**
   * Constructor.
   *
   * @param status the {@link HttpStatus} of the error
   * @param message the message describing the reason for the error
   */
  public ErrorResponse(HttpStatus status, String message) {
    this.status = status.value();
    this.message = message;
  }
}
