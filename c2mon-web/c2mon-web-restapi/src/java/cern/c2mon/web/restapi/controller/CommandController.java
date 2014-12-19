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
package cern.c2mon.web.restapi.controller;

import static cern.c2mon.web.restapi.version.ApiVersion.API_V1;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.web.restapi.exception.UnknownResourceException;
import cern.c2mon.web.restapi.service.CommandService;

/**
 * @author Justin Lewis Salmon
 */
@Controller
public class CommandController {
  /**
  *
  */
 private static final String COMMAND_VALUE_MAPPING = "/commands/{id}";

 /**
  *
  */
 @Autowired
 private CommandService service;

 /**
  *
  * @param id
  * @return
  * @throws UnknownResourceException
  */
 @RequestMapping(value = COMMAND_VALUE_MAPPING, method = GET, produces = { API_V1 })
 @ResponseBody
 public ClientCommandTag<?> getCommand(@PathVariable final Long id) throws UnknownResourceException {
   return service.getCommand(id);
 }
}
