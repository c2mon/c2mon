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

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.web.restapi.exception.UnknownResourceException;
import cern.c2mon.web.restapi.service.HistoryService;

/**
 * Controller entry point for all requests for historical data of all supported
 * resource types.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class HistoryController {

  /**
   * The URL mapping to be used for retrieving historical data. The mapping
   * contains a regular expression to allow the same method to be invoked for
   * multiple resource types. The type will be passed by Spring into the request
   * mapping method as a parameter.
   *
   * <p>
   * Note: Historical data is only available for data tags and alarms (not
   * commands).
   * </p>
   */
  private static final String HISTORY_MAPPING = "/{type:datatags|alarms}/{id}/history";

  /**
   * Request parameter for a specific number of records of a resource.
   */
  private static final String RECORDS = "records";

  /**
   * Request parameter for all records of a resource for specific number of
   * days.
   */
  private static final String DAYS = "days";

  /**
   * Request parameters for all records of a resource between a pair of dates.
   */
  private static final String FROM = "from";
  private static final String TO = "to";

  /**
   * The date format pattern that the client must provide with a from/to
   * request.
   */
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /**
   * Reference to the history service bean.
   */
  @Autowired
  private HistoryService service;

  /**
   * Request mapping entry point for a request for a specific number of records
   * of historical data for a specific resource.
   *
   * @param type the type of resource requested (datatags, alarms, etc.)
   * @param id the ID of the requested resource
   * @param records the number of records requested
   *
   * @return a list of {@link HistoryTagValueUpdate} objects, which will be
   *         automatically serialised by Spring
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = RECORDS)
  @ResponseBody
  public List<HistoryTagValueUpdate> getHistory(@PathVariable final String type,
                                                @PathVariable final Long id,
                                                @RequestParam(value = RECORDS) final Integer records)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, records);
  }

  /**
   * Request mapping entry point for a request for all historical records for a
   * specific number of days for a specific resource.
   *
   * @param type the type of resource requested (datatags, alarms, etc.)
   * @param id the ID of the requested resource
   * @param days the number of days requested
   *
   * @return a list of {@link HistoryTagValueUpdate} objects, which will be
   *         automatically serialised by Spring
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = DAYS)
  @ResponseBody
  public List<HistoryTagValueUpdate> getHistory(@PathVariable final String type, @PathVariable final Long id, @RequestParam(value = DAYS) final String days)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, days);
  }

  /**
   * Request mapping entry point for a request for all historical records of a
   * resource between two dates for a specific resource.
   *
   * @param type the type of resource requested (datatags, alarms, etc.)
   * @param id the ID of the requested resource
   * @param from the beginning date
   * @param to the ending date
   *
   * @return a list of {@link HistoryTagValueUpdate} objects, which will be
   *         automatically serialised by Spring
   *
   * @throws LoadingParameterException if an error occurs retrieving the history
   * @throws UnknownResourceException if no resource was found with the given ID
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = { FROM, TO })
  @ResponseBody
  public List<HistoryTagValueUpdate> getHistory(@PathVariable final String type,
                                                @PathVariable final Long id,
                                                @RequestParam(value = FROM) @DateTimeFormat(pattern = DATE_PATTERN) final Date from,
                                                @RequestParam(value = TO) @DateTimeFormat(pattern = DATE_PATTERN) final Date to)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, from, to);
  }
}
