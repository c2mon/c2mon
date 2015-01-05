/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2014 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.restapi.exception;

import org.springframework.http.HttpStatus;

/**
 * This class is used to hold a HTTP status and an error message which will be
 * serialised and returned to clients when errors occur during API requests.
 *
 * @author Justin Lewis Salmon
 */
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

  /**
   * Retrieve the HTTP status code.
   *
   * @return the status code
   */
  public int getStatus() {
    return status;
  }

  /**
   * Retrieve the error message string.
   *
   * @return the message
   */
  public String getMessage() {
    return message;
  }
}
