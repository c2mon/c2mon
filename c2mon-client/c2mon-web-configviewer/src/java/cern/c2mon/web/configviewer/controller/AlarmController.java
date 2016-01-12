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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.configviewer.service.AlarmService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;

/**
 * A controller for the alarm viewer
 **/
@Controller
public class AlarmController {

  /**
   * A REST-style URL to alarmviewer, combined with alarm id displays alarm information
   */
  public static final String ALARM_URL = "/alarmviewer/";

  /**
   * A REST-style URL to alarmviewer, combined with alarm id displays alarm information
   * in RAW XML
   */
  public static final String ALARM_XML_URL = "/alarmviewer/xml";

  /**
   * A URL to the alarmviewer with input form
   */
  public static final String ALARM_FORM_URL = "/alarmviewer/form";

  /**
   * Title for the alarm form page
   */
  public static final String ALARM_FORM_TITLE = "Alarm Configuration Viewer";

  /**
   * Description for the alarm form page
   */
  public static final String ALARM_FORM_INSTR = "Enter an alarm id.";

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.trend.viewer.help.url:}")
  public String helpUrl;

  /**
   * An alarm service
   */
  @Autowired
  private AlarmService service;

  /**
   * AlarmController logger
   */
  private static Logger logger = LoggerFactory.getLogger(AlarmController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = ALARM_URL, method = { RequestMethod.GET })
  public String viewAlarm(final Model model) {
    logger.info("/alarmviewer/");
    return ("redirect:" + "/alarmviewer/form");
  }

  /**
   * @return Displays alarm information in RAW XML about a tag with the given id.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = ALARM_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    logger.info(ALARM_XML_URL + id);
    try {
      model.addAttribute("xml", service.getAlarmTagXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/alarmviewer/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return
   * Displays alarm information for a given alarm id.
   *
   * @param id alarm id
   * @param response we write the html result to that HttpServletResponse response
   */
  @RequestMapping(value = ALARM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewAlarm(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request)
      throws IOException  {
    logger.info(ALARM_URL + id);

    AlarmValue alarm = service.getAlarmValue(new Long(id));
    if (alarm == null) {
      return ("redirect:" + "/alarmviewer/errorform/" + id);
    }

    model.addAttribute("alarm", alarm);
    model.addAttribute("title", ALARM_FORM_TITLE);
    model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarm.getTagId().toString()));
    return "alarm";
  }

  /**
   * @return
   * Displays a form where an alarm id can be entered.
   *
   * @param id alarm id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/alarmviewer/form/{id}", method = { RequestMethod.GET })
  public String viewAlarmWithForm(@PathVariable final String id, final Model model) {
    logger.info("/alarmviewer/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, id, ALARM_URL + id));
    return "genericForm";
  }

  /**
   * @return In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   *
   * New queries are simply redirected to ALARM_URL + id
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/alarmviewer/errorform/{id}")
  public String viewAlarmErrorForm(@PathVariable(value = "id") final String errorId,
      @RequestParam(value = "id", required = false) final String id, final Model model) {

    logger.info("/alarmviewer/errorform " + id);

    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, null, null));
    else
      return ("redirect:" + ALARM_URL + id);

    model.addAttribute("err", errorId);
    return "genericErrorForm";
  }

  /**
   * @return
   * Displays an input form for an alarm id,
   * and if a POST was made with an alarm id, it is simply redirected to ALARM_URL + id
   *
   * @param id alarm id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/alarmviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewAlarmFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/alarmviewer/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, null, null));
    else
      return ("redirect:" + ALARM_URL + id);

    return "genericForm";
  }
}
