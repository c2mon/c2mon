/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.web.manager.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * @author Justin Lewis Salmon
 */
@Controller
public class ErrorController implements org.springframework.boot.autoconfigure.web.ErrorController {

  private static final String PATH = "/error";

  @Autowired
  private ErrorAttributes errorAttributes;

  @RequestMapping(value = PATH)
  public String error(HttpServletRequest request, HttpServletResponse response) {
    Map<String, Object> attributes = errorAttributes.getErrorAttributes(new ServletRequestAttributes(request), true);
    Integer status = (Integer) attributes.get("status");

    if (status == 404) {
      return "error/404";
    }

    return null;
  }

  @Override
  public String getErrorPath() {
    return PATH;
  }
}
