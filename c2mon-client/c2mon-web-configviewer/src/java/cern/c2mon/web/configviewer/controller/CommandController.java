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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.web.configviewer.service.CommandService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the command viewer
 */
@Controller
public class CommandController {

  /**
   * A REST-style URL to commandviewer, combined with command id displays command information
   */
  public static final String COMMAND_URL = "/commandviewer/";

  /**
   * URL to commandviewer, that displays command information in RAW XML
   */
  public static final String COMMAND_XML_URL = "/commandviewer/xml";

  /**
   * A URL to the commandviewer with input form
   */
  public static final String COMMAND_FORM_URL = "/commandviewer/form";

  /**
   * Title for the command form page
   */
  public static final String COMMAND_FORM_TITLE = "Command Configuration Viewer";

  /**
   * Description for the command form page
   */
  public static final String COMMAND_FORM_INSTR = "Enter a command id to view the command's configuration.";

  /**
   * A command service
   */
  @Autowired
  private CommandService service;

  /**
   * CommandController logger
   */
  private static Logger logger = LoggerFactory.getLogger(CommandController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = "/commandviewer/", method = { RequestMethod.GET })
  public String viewCommand(final Model model) {
    logger.info("/commandviewer/");
    return ("redirect:" + "/commandviewer/form");
  }

  /**
   * @return
   * Displays command information in RAW XML about a tag with the given id.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = COMMAND_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    logger.info(COMMAND_XML_URL + id);
    try {
      model.addAttribute("xml", service.getCommandTagXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/commandviewer/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return
   * Displays command information for a given command id.
   *
   * @param id command id
   * @param response we write the html result to that HttpServletResponse response
   */
  @RequestMapping(value = "/commandviewer/{id}", method = { RequestMethod.GET })
  public String viewCommand(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model) throws IOException  {
    logger.info("/commandviewer/{id} " + id);

    ClientCommandTag<?> tag = service.getCommandTag(new Long(id));
    if (tag == null) {
      return ("redirect:" + "/" + COMMAND_URL + "/errorform/" + id);
    }

    model.addAttribute("title", COMMAND_FORM_TITLE);
    model.addAttribute("tag", tag);
    return "commandtag";
  }

  /**
   * @return
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * New queries are redirected to COMMAND_URL + id
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/commandviewer/errorform/{id}")
  public String viewCommandErrorForm(@PathVariable(value = "id") final String errorId,
      @RequestParam(value = "id", required = false) final String id, final Model model) {

    logger.info("/commandviewer/errorform " + id);

    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR, COMMAND_FORM_URL, id, COMMAND_URL + id));
    else
      return ("redirect:" + COMMAND_URL + id);

    model.addAttribute("err", errorId);
    return "genericErrorForm";
  }

  /**
   * @return
   * Displays a form where an command id can be entered.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/commandviewer/form/{id}", method = { RequestMethod.GET })
  public String viewCommandWithForm(@PathVariable final String id, final Model model) {
    logger.info("/commandviewer/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR,
        COMMAND_FORM_URL, id, COMMAND_URL + id));
    return "genericForm";
  }

  /**
   * @return name of a jsp page which will be displayed
   * Displays an input form for a command id, and if a POST was made with a command id,
   * redirects to COMMAND_URL + id.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/commandviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewCommandFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/commandviewer/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR, COMMAND_FORM_URL, null, null));
    else
      return ("redirect:" + COMMAND_URL + id);
    return "genericForm";
  }
}
