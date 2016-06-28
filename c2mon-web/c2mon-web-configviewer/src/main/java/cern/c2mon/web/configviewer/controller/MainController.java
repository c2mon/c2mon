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
package cern.c2mon.web.configviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 *
 * This is a super simple html page (we also add the username of the user to
 * show a personalised message once he is logged in).
 */
@Controller
@RequestMapping(value = "/")
public class MainController {

  /**
   * This is the application home page.
   *
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(method = RequestMethod.GET)
  public String getCreateForm() {
    return "home";
  }

  /**
   * Returns a custom 404 error page.
   *
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping("error/404")
  public String error404(final Model model) {
    model.addAttribute("errorMessage", "");
    model.addAttribute("title", "");
    return "error/404";
  }
}
