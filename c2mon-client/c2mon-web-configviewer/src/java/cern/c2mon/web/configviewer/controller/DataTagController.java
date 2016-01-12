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

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.service.TagService;
import cern.c2mon.web.configviewer.util.FormUtility;

/**
 * A controller for the datatag viewer
 */
@Controller
public class DataTagController {


  private static final String TAGVIEWER_PAGE_NAME = "tagviewer";

  /**
   * A REST-style URL to tagviewer, combined with tag id displays datatag configuration
   */
  private static final String TAG_URL = "../";

  /**
   * A URL to the tagviewer with input form
   */
  public static final String TAG_FORM_URL = "/" + TAGVIEWER_PAGE_NAME + "/form";

  /**
   * URL to tagviewer, which displays tagconfig information
   * in RAW XML
   */
  public static final String TAG_CONFIG_XML_URL = "/tagconfig/xml";

  /**
   * URL to tagviewer, which displays tag value information
   * in RAW XML
   */
  public static final String TAG_VALUE_XML_URL = "/tagvalue/xml";

  /**
   * Title for the datatag form page
   */
  public static final String TAG_FORM_TITLE = "Tag Viewer";

  /**
   * Description for the datatag form page
   */
  public static final String TAG_FORM_INSTR = "Enter a tag id to view the tag's configuration.";

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.trend.viewer.help.url:}")
  public String helpUrl;

  /**
   * A datatag service
   */
  @Autowired
  private TagService service;

  /**
   * DataTagController logger
   */
  private static Logger logger = LoggerFactory.getLogger(DataTagController.class);


  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/", method = { RequestMethod.GET })
  public String viewTag(final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/");
    return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/form");
  }

  /**
   * @return
   * Displays (TagConfig + TagValue) information for a tag with the specified id.
   *
   * @param id tag id
   * @param response we write the html result to that HttpServletResponse response
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/{id}", method = { RequestMethod.GET })
  public String viewTag(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException  {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/{id} " + id);

    ClientDataTagValue tag = service.getDataTagValue(new Long(id));
    if (tag == null) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }

    model.addAttribute("title", TAG_FORM_TITLE);
    model.addAttribute("tag", tag);
    model.addAttribute("tagConfig", service.getTagConfig(new Long(id)));
    model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", tag.getId().toString()));
    return "datatag";
  }

  /**
   * @return
   * Displays TagConfig information in RAW XML about a tag with the given id.
   *
   * In case the TagId does not exist, redirects to an error form.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = TAG_CONFIG_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagConfigXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_CONFIG_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagConfigXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return
   * Displays TagValue information in RAW XML about a tag with the given id.
   *
   * In case the TagId does not exist, redirects to an error form.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = TAG_VALUE_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagValueXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_VALUE_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagValueXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/errorform/{id}", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagErrorForm(@PathVariable(value = "id") final String errorId,
      @RequestParam(value = "id", required = false) final String id, final Model model) {

    logger.info("/" + TAGVIEWER_PAGE_NAME + "/errorform " + id);

    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE,
          TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
    else {
      return ("redirect:" + TAG_URL + id);
    }

    model.addAttribute("err", errorId);
    return "genericErrorForm";
  }

  /**
   * Displays a form where a datatag id can be entered.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form/{id}", method = { RequestMethod.GET })
  public String viewTagWithForm(@PathVariable final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, id, TAG_URL + id));
    return "genericForm";
  }

  /**
   * Displays an input form for a datatag id, and if a POST was made with a datatag id, also the datatag information.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form " + id);
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
     else {
       return ("redirect:" + TAG_URL + TAGVIEWER_PAGE_NAME + "/" + id);
     }

    return "genericForm";
  }
}
