/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * <p>
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
 * <p>
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.web.restapi.controller;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cern.c2mon.web.restapi.exception.UnknownResourceException;

/**
 * Default controller entry point for all API requests that do not map to any
 * other controller.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class DefaultController implements ErrorController {

  private static final String ERROR_PATH = "/error";

  /**
   * When a request is made to the API root, show a HTML page with some
   * documentation about the API.
   */
  @RequestMapping("/")
  public String index() {
    return "index";
  }

  /**
   * When a request is made to an unmapped URL, simply throw an
   * UnknownResourceException, which will be handled by Spring and converted
   * into a proper JSON error response.
   */
  @RequestMapping(ERROR_PATH)
  public String unknownResource() throws UnknownResourceException {
    throw new UnknownResourceException("There is no resource at the requested location");
  }

  @Override
  public String getErrorPath() {
    return null;
  }
}
