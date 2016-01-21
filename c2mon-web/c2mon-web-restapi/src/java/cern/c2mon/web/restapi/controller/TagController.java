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

import static cern.c2mon.web.restapi.version.ApiVersion.API_V1;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.client.tag.TagConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.web.restapi.exception.UnknownResourceException;
import cern.c2mon.web.restapi.service.TagService;

/**
 * Controller entry point for data tag API requests.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class TagController {

  /**
   * The URL mapping to be used for retrieving data tags.
   */
  private static final String DATATAG_VALUE_MAPPING = "/datatags/{id}";

  /**
   * Reference to the data tag service bean.
   */
  @Autowired
  private TagService service;

  /**
   * Spring MVC request mapping entry point for requests to the URL defined by
   * DATATAG_VALUE_MAPPING.
   *
   * <p>
   * Note: only GET requests are allowed to this URL.
   * </p>
   *
   * @param id the path variable representing the ID of the data tag to be retrieved
   * @return the {@link Tag} object itself, which will be automatically serialised by Spring
   *
   * @throws UnknownResourceException if no data tag was found with the given ID
   */
  @RequestMapping(value = DATATAG_VALUE_MAPPING, method = GET, produces = { API_V1 })
  @ResponseBody
  public Tag getTag(@PathVariable final Long id) throws UnknownResourceException {
    return service.getTag(id);
  }

  @RequestMapping(value = DATATAG_VALUE_MAPPING + "/config", method = GET, produces = { API_V1 })
  @ResponseBody
  public TagConfig getTagConfig(@PathVariable final Long id) throws UnknownResourceException {
    return service.getTagConfig(id);
  }
}
