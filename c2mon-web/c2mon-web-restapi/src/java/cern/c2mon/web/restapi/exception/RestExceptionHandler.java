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

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;

/**
 * @author Justin Lewis Salmon
 */
@ControllerAdvice
public class RestExceptionHandler {

  /**
   *
   * @param e
   * @return
   */
  @ExceptionHandler(Exception.class)
  @ResponseBody
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Object handleException(Exception e) {
    return new ErrorResponse(HttpStatus.BAD_REQUEST, e);
  }

  /**
   *
   * @param e
   * @return
   */
  @ExceptionHandler(TypeMismatchException.class)
  @ResponseBody
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Object handleTypeMismatchException(Exception e) {
    return new ErrorResponse(HttpStatus.BAD_REQUEST, "Bad request parameter format");
  }

  /**
   *
   * @param e
   * @return
   */
  @ExceptionHandler(UnknownResourceException.class)
  @ResponseBody
  @ResponseStatus(value = HttpStatus.NOT_FOUND)
  public Object handleUnknownResourceException(UnknownResourceException e) {
    return new ErrorResponse(HttpStatus.NOT_FOUND, e);
  }

  /**
   *
   * @param e
   * @return
   */
  @ExceptionHandler(HistoryProviderException.class)
  @ResponseBody
  @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
  public Object handleHistoryProviderException(HistoryProviderException e) {
    return new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, e);
  }

  /**
   *
   * @param e
   * @return
   */
  @ExceptionHandler(LoadingParameterException.class)
  @ResponseBody
  @ResponseStatus(value = HttpStatus.BAD_REQUEST)
  public Object handleLoadingParameterException(LoadingParameterException e) {
    return new ErrorResponse(HttpStatus.BAD_REQUEST, e);
  }
}
