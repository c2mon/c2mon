/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
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
 *****************************************************************************/
package cern.c2mon.client.ext.history.playback.exceptions;

/**
 * Is thrown to indicate that no {@link HistoryProvider} is available
 * 
 * @author vdeila
 */
public class NoHistoryProviderAvailableException extends Exception {

  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = -8467068588031734986L;

  /** Empty constructor */
  public NoHistoryProviderAvailableException() {
  }

  /**
   * 
   * @param message
   *          an description of why the exception occurred
   */
  public NoHistoryProviderAvailableException(final String message) {
    super(message);
  }

  /**
   * 
   * @param cause
   *          the original exception
   */
  public NoHistoryProviderAvailableException(final Throwable cause) {
    super(cause);
  }

  /**
   * 
   * @param message
   *          an description of why the exception occurred
   * @param cause
   *          the original exception
   */
  public NoHistoryProviderAvailableException(final String message, final Throwable cause) {
    super(message, cause);
  }

}
