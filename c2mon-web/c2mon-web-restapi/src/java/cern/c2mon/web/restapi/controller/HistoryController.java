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
 * @author Justin Lewis Salmon
 */
@Controller
public class HistoryController {

  /**
   * Historical data is only available for datatags and alarms (not commands).
   */
  private static final String HISTORY_MAPPING = "/{type:datatags|alarms}/{id}/history";

  private static final String RECORDS = "records";
  private static final String DAYS = "days";
  private static final String FROM = "from";
  private static final String TO = "to";

  /**
   *
   */
  private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /**
   *
   */
  @Autowired
  private HistoryService service;

  /**
   *
   * @param id
   * @param records
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = "records")
  @ResponseBody
  public List<HistoryTagValueUpdate> getDataTagHistoryByRecords(@PathVariable final String type,
                                                                @PathVariable final Long id,
                                                                @RequestParam(value = RECORDS) final Integer records)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, records);
  }

  /**
   *
   * @param id
   * @param days
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = "days")
  @ResponseBody
  public List<HistoryTagValueUpdate> getDataTagHistoryByDays(@PathVariable final String type,
                                                             @PathVariable final Long id,
                                                             @RequestParam(value = DAYS) final String days)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, days);
  }

  /**
   *
   * @param id
   * @param from
   * @param to
   * @return
   * @throws LoadingParameterException
   * @throws UnknownResourceException
   */
  @RequestMapping(value = HISTORY_MAPPING, method = GET, produces = { API_V1 }, params = { "from", "to" })
  @ResponseBody
  public List<HistoryTagValueUpdate> getDataTagHistoryByDateTimeRange(@PathVariable final String type,
                                                                      @PathVariable final Long id,
                                                                      @RequestParam(value = FROM) @DateTimeFormat(pattern = DATE_PATTERN) final Date from,
                                                                      @RequestParam(value = TO) @DateTimeFormat(pattern = DATE_PATTERN) final Date to)
      throws LoadingParameterException, UnknownResourceException {
    return service.getHistory(id, type, from, to);
  }
}
