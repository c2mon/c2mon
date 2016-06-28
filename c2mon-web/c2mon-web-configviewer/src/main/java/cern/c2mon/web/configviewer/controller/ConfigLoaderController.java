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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cern.c2mon.shared.client.configuration.ConfigConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.support.PagedListHolder;
//import org.springframework.security.acls.model.NotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.shared.client.configuration.ConfigurationElementReport;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.shared.util.json.GsonFactory;
import cern.c2mon.web.configviewer.service.ConfigLoaderService;
import cern.c2mon.web.configviewer.util.FormUtility;

import com.google.gson.Gson;

/**
 * A controller for the ConfigLoader
 */
@Controller
public class ConfigLoaderController {

  /** Used to convert the returned value into JSON format for the AJAX calls */
  private static transient Gson gson = null;

  /**
   * A REST-style URL
   */
  public static final String CONFIG_LOADER_URL = "/configloader/";

  /**
   * A URL to the config report viewer with input form
   */
  public static final String CONFIG_LOADER_FORM_URL = CONFIG_LOADER_URL + "form";

  /**
   * URL for ajax progress report requests.
   */
  public static final String CONFIG_LOADER_PROGRESS_REPORT_URL = CONFIG_LOADER_URL + "progress";

  /**
   * URL that retrieves a Stored Configuration Report and displays it.
   */
  public static final String CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL = CONFIG_LOADER_PROGRESS_REPORT_URL + "/finalReport/";

  /**
   * URL that retrieves a Stored Configuration Report and displays it in RAW
   * XML.
   */
  public static final String CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL = CONFIG_LOADER_PROGRESS_REPORT_URL + "/finalReport/" + "xml";

  /**
   * Title for the config form page
   */
  public static final String CONFIG_LOADER_FORM_TITLE = "Configuration Loader";

  /**
   * Description for the config form page
   */
  public static final String CONFIG_LOADER_FORM_INSTR = "Please enter the Configuration ID you want to apply.";

  /**
   * A config loader service
   */
  @Autowired
  private ConfigLoaderService service;

  /**
   * ConfigLoaderController logger
   */
  private static Logger logger = LoggerFactory.getLogger(ConfigLoaderController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = CONFIG_LOADER_URL, method = { RequestMethod.GET })
  public String viewConfig(final Model model) {
    logger.debug(CONFIG_LOADER_URL);
    return ("redirect:" + "/configloader/form");
  }

  /**
   * @return Displays an --ALREADY APPLIED-- configuration report in RAW XML
   *         format.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id, final Model model) {
    logger.debug(CONFIG_LOADER_PROGRESS_FINAL_REPORT_XML_URL + id);

    try {
      model.addAttribute("xml", service.getConfigurationReports(id).get(0).toXML());
    } catch (RuntimeException e) {
      return ("redirect:" + "/configloader/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * Retrieves a list of stored {@link ConfigurationReport} objects for a given
   * configuration and displays it.
   *
   * @param id the report id
   * @param timestamp the timestamp of a specific report (used for paging)
   * @param page the page number to display
   * @param showSuccesses
   * @param showWarnings
   * @param showFailures
   * @param model
   *
   * @return
   * @throws IOException
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL + "{id}", method = { RequestMethod.GET })
  public String viewFinalReport(@PathVariable(value = "id") final String id,
                                @RequestParam(value = "ts", required = false) Timestamp timestamp,
                                @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                @RequestParam(value = "s", required = false, defaultValue = "true") boolean showSuccesses,
                                @RequestParam(value = "w", required = false, defaultValue = "true") boolean showWarnings,
                                @RequestParam(value = "f", required = false, defaultValue = "true") boolean showFailures,
                                final Model model) throws IOException {
    logger.debug(CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL + "/{id} " + id);

    List<ConfigurationReport> reports = service.getConfigurationReports(id);
    ConfigurationReport report = null;

    if (timestamp == null) {
      report = reports.get(reports.size() - 1);
    }

    for (ConfigurationReport r : reports) {
      if (r.getTimestamp().equals(timestamp)) {
        report = r;
      }
    }

    if (report == null) {
      throw new RuntimeException("Report id " + id + " with timestamp " + timestamp + " was not found.");
    }

    List<ConfigurationElementReport> elementReports = new ArrayList<>();
    for (ConfigurationElementReport elementReport : report.getElementReports()) {
      if (elementReport.getStatus() == ConfigConstants.Status.OK && showSuccesses) elementReports.add(elementReport);
      if (elementReport.getStatus() == ConfigConstants.Status.WARNING && showWarnings) elementReports.add(elementReport);
      if (elementReport.getStatus() == ConfigConstants.Status.FAILURE && showFailures) elementReports.add(elementReport);
    }

    PagedListHolder<ConfigurationElementReport> pagedListHolder = new PagedListHolder<>(elementReports);
    pagedListHolder.setPage(page);
    pagedListHolder.setPageSize(50);

    logger.debug(reports.toString());
    model.addAttribute("report", report);
    model.addAttribute("allReports", reports);
    model.addAttribute("pagedListHolder", pagedListHolder);
    model.addAttribute("showSuccesses", showSuccesses);
    model.addAttribute("showWarnings", showWarnings);
    model.addAttribute("showFailures", showFailures);
    model.addAttribute("title", "Configuration Report: " + id);

    return "config/configReport";
  }

  /**
   * @return In case of an error this form is shown. It displays the error and
   *         you can also make a new query.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = "/configloader/errorform/{id}")
  public String viewConfigLoaderErrorForm(@PathVariable(value = "id") final String errorId,
                                          @RequestParam(value = "id", required = false) final String id,
                                          final Model model) {
    logger.debug("/configloader/errorform " + id);

    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, null, null));
    } else {
      return ("redirect:" + CONFIG_LOADER_URL + id);
    }

    model.addAttribute("err", errorId);
    return "genericErrorForm";
  }

  /**
   * @return Displays a form where a config id can be entered.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewConfigLoaderWithForm(@PathVariable final String id, final Model model) {
    logger.debug("/configloader/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, id, CONFIG_LOADER_URL + id));

    return "genericForm";
  }

  /**
   * @return Displays an input form for a config id.
   *
   *         If a POST was made with a config id, redirects to CONFIG_LOADER_URL
   *         + id.
   *
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewConfigLoaderFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.debug("/configloader/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, null, null));
    else
      return ("redirect:" + CONFIG_LOADER_URL + id);
    return "genericForm";
  }

  /**
   * @return Displays an input form for a configuration id. If a request is made
   *         from this form, a listener is registered that listens for
   *         ProgressReport updates.
   *
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL, method = RequestMethod.GET)
  public String startConfigurationProcessWithProgressReportForm(final Model model) {
    logger.debug(CONFIG_LOADER_PROGRESS_REPORT_URL);

    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, "", CONFIG_LOADER_URL));
    return "loadConfigForm";
  }

  /**
   * Starts an applyConfiguration request to the server. Listens for Progress
   * Report updates.
   *
   * @param configurationId the id of the configuration
   * @throws Exception
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/start", method = RequestMethod.POST)
  public void startConfigurationProcess(@RequestParam("configurationId") final String configurationId) throws Exception {
    logger.debug("(AJAX) Starting Configuration Request: " + configurationId);
    service.applyConfiguration(Integer.parseInt(configurationId));
  }

  /**
   * @param configurationId the id of the configuration
   * @return Returns the current progress of the request.
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/getProgress", method = RequestMethod.POST)
  @ResponseBody
  public Integer getProgressReport(@RequestParam("configurationId") final String configurationId) throws InterruptedException {
    logger.debug("(AJAX) Received Progress Report Request for configurationId:" + configurationId);

    ClientRequestProgressReport report = service.getProgressReportForConfiguration(configurationId);
    if (report == null) {
      return 0;
    }

    int currentProgress = 0;
    if (report.getTotalProgressParts() > 0) {
      currentProgress = ((100 * report.getCurrentProgressPart()) / report.getTotalProgressParts());
      logger.debug("returning:" + report.getCurrentProgressPart() + " out of " + report.getTotalProgressParts());
    } else {
      currentProgress = 100;
    }

    // @ResponseBody will automatically convert the returned value into JSON
    return currentProgress;
  }

  /**
   * @param configurationId the id of the configuration
   * @return Returns a description of what is happening in the server currently
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/getProgressDescription", method = RequestMethod.POST)
  @ResponseBody
  public String getProgressDescription(@RequestParam("configurationId") final String configurationId) throws InterruptedException {
    logger.debug("(AJAX) Received Progress Description Request for configurationId:" + configurationId);

    ClientRequestProgressReport report = service.getProgressReportForConfiguration(configurationId);
    String progressDescription = null;

    if (report != null) {
      progressDescription = report.getProgressDescription();
    }
    // @ResponseBody will automatically convert the returned value into JSON
    // Jackson does not work as expected in this case.. so Gson is used for this
    // case
    return getGson().toJson(progressDescription);
  }

  /**
   * @return The Gson parser singleton instance
   */
  protected static synchronized Gson getGson() {
    if (gson == null) {
      gson = GsonFactory.createGson();
    }
    return gson;
  }
}
