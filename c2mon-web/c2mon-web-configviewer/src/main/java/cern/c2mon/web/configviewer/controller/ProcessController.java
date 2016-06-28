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

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.configviewer.service.ProcessService;
import cern.c2mon.web.configviewer.service.TagIdException;

/**
 * A controller for the daq process viewer.
 */
@Controller
public class ProcessController {

  /** A REST-style URL */
  public static final String PROCESS_URL = "/process/";

  /** URL for the process form. */
  public static final String PROCESS_FORM_URL = PROCESS_URL + "form";

  /** Displays the configuration of a process in RAW xml format */
  public static final String PROCESS_XML_URL = PROCESS_URL + "xml";

  /**
   * Title for the process form page
   */
  public static final String PROCESS_FORM_TITLE = "DAQ Process Viewer";

  /** Description for the process form page */
  public static final String PROCESS_FORM_INSTR = "Select process Name.";

  /** process loader service */
  @Autowired
  private ProcessService service;

  /**
   * ProcessControler logger
   */
  private static Logger logger = LoggerFactory.getLogger(ProcessController.class);

  /**
   * Displays configuration of a process with the given process name
   *
   * @param processName the process name
   * @param response we write the html result to that HttpServletResponse
   *          response
   * @throws Exception
   */
  @RequestMapping(value = PROCESS_URL + "/{processName}", method = { RequestMethod.GET })
  public String viewProcess(@PathVariable(value = "processName") final String processName, final HttpServletResponse response, final Model model)
      throws Exception {
    logger.info(PROCESS_URL + processName);

    ProcessConfiguration process = service.getProcessConfiguration(processName);
    process.setProcessName(processName);

    model.addAttribute("title", PROCESS_FORM_TITLE);
    model.addAttribute("process", process);
    return "process";
  }

  /**
   * @return Displays configuration of a process with the given id together with
   *         a form
   *
   * @param id the process name
   */
  @RequestMapping(value = PROCESS_FORM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewProcessWithForm(@PathVariable final String id, final Model model) {
    logger.info("/process/form/{id} " + id);
    model.addAllAttributes(getProcessFormModel(PROCESS_FORM_TITLE, PROCESS_FORM_INSTR, PROCESS_FORM_URL, id, PROCESS_URL + id));
    return "viewProcessForm";
  }

  /**
   * @return Displays configuration of a process with the given process name in
   *         RAW XML format
   *
   * @param id process name
   * @throws Exception
   */
  @RequestMapping(value = PROCESS_XML_URL + "/{id}", method = { RequestMethod.GET })
  public void viewXml(@PathVariable final String id, final HttpServletResponse response) throws Exception {
    logger.info("/process/xml/{id} " + id);
    InputStream is = IOUtils.toInputStream(service.getProcessXml(id), "UTF-8");
    IOUtils.copy(is, response.getOutputStream());
    response.flushBuffer();
  }

  /**
   * @return Displays an input form.
   *
   *         If a POST was made, redirects to the PROCESS_URL.
   *
   * @param id process name
   */
  @RequestMapping(value = PROCESS_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewProcessFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {

    logger.info("/process/form " + id);
    if (id == null) {
      model.addAllAttributes(getProcessFormModel(PROCESS_FORM_TITLE, PROCESS_FORM_INSTR, PROCESS_FORM_URL, null, null));
    } else {
      return ("redirect:" + PROCESS_URL + id);
    }

    return "viewProcessForm";
  }

  /**
   * Returns a map of values to include later in the MVC model processed by a
   * jsp. This form is used for the process form and includes a collection of
   * all available process names in addition to the other forms.
   *
   * @param title tag title of the form displayed on the jsp page (different for
   *          datatag, alarm, command)
   * @param instruction description of the user action displayed on the jsp page
   * @param formSubmitUrl url to which the form should be submitted
   * @param formTagValue previous value of a tag (datatag, alarm, command)
   *          entered in the form, that should be displayed in the form
   * @return a map of values ready to be used in the MVC model
   */
  public Map<String, Object> getProcessFormModel(final String title, final String instruction, final String formSubmitUrl, final String formTagValue,
      final String tagDataUrl) {

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("title", title);
    model.put("instruction", instruction);
    model.put("formSubmitUrl", formSubmitUrl);
    model.put("formTagValue", formTagValue);
    model.put("tagDataUrl", tagDataUrl);

    try {
      Collection<String> names = service.getProcessNames();

      if (names instanceof List) {
        Collections.sort((List) names, String.CASE_INSENSITIVE_ORDER);
      } else {
        logger.warn(new String("getProcessFormModel(): getProcessNames() does not return " + "a list anymore! "
            + "This means you should provide some other way to sort the Collection returned" + "by this call."));
      }

      model.put("processNames", names);

    } catch (Exception e) {
      model.put("tagErr", "Unexpected problem occured. Try again or contact C2MON support");
      logger.error("Unexpected problem occured while getting the XML:", e);
    }

    return model;
  }

  /**
   * A helper method to get the xml and build a map of values for the MVC model.
   *
   * @param processName the process name
   * @return a map of values to include in the model for a jsp page.
   */
  private Map<String, Object> getProcessModel(final String processName) {

    Map<String, Object> model = new HashMap<String, Object>();

    try {
      String processXml = service.getProcessXml(processName);
      Collection<String> names = service.getProcessNames();

      model.put("processXml", processXml);
      model.put("processNames", names);

    } catch (TagIdException e) {
      model.put("tagErr", e.getMessage());
      logger.error(e.toString());
    } catch (Exception e) {
      model.put("tagErr", "Unexpected problem occured. Try again or contact C2MON support");
      logger.error("Unexpected problem occured while getting the XML:", e);
    }
    return model;
  }
}
