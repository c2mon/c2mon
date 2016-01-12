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
