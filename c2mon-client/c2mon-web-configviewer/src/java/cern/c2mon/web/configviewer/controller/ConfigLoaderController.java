package cern.c2mon.web.configviewer.controller;

import java.io.IOException;
import java.util.Random;

import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingUtilities;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import cern.c2mon.shared.client.request.ClientRequestErrorReport;
import cern.c2mon.shared.client.request.ClientRequestErrorReportImpl;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.web.configviewer.service.ConfigLoaderService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the ConfigLoader 
 * */
@Controller
public class ConfigLoaderController {

  /**
   * A REST-style URL 
   * */
  public static final String CONFIG_LOADER_URL = "/configloader/";

  /**
   * A URL to the config report viewer with input form
   * */
  public static final String CONFIG_LOADER_FORM_URL = CONFIG_LOADER_URL + "form";

  /**
   * A REST-style URL to config report viewer, which displays config reports
   * in RAW XML
   */
  public static final String CONFIG_LOADER_XML_URL = CONFIG_LOADER_URL + "xml";

  /**
   * URL for ajax progress report requests.
   */
  public static final String CONFIG_LOADER_PROGRESS_REPORT_URL = CONFIG_LOADER_URL + "progress";

  /**
   * URL for ajax progress report requests.
   */
  public static final String CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL = 
    CONFIG_LOADER_PROGRESS_REPORT_URL + "/finalReport/";

  /**
   * Title for the config form page
   * */
  public static final String CONFIG_LOADER_FORM_TITLE = "Config Loader";

  /**
   * Description for the config form page
   * */
  public static final String CONFIG_LOADER_FORM_INSTR = "Enter configuration id.";

  /**
   * A config loader service
   * */
  @Autowired
  private ConfigLoaderService service;

  /**
   * ConfigLoaderController logger
   * */
  private static Logger logger = Logger.getLogger(ConfigLoaderController.class);

  /**
   * Displays configuration for the given id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = CONFIG_LOADER_URL, method = { RequestMethod.GET })
  public String viewConfig(final Model model) {
    logger.info(CONFIG_LOADER_URL);
    return ("redirect:" + "/configloader/form");
  }    

  /**
   * Displays configuration in RAW XML format.
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = CONFIG_LOADER_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    logger.info(CONFIG_LOADER_XML_URL + id);
    try {
      model.addAttribute("xml", service.getConfigurationReportXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/commandviewer/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }

  /**
   * Displays configuration for the given id.
   * @param id config id
   * @param response we write the html result to that HttpServletResponse response
   * @return nothing
   * @throws IOException 
   * */
  @RequestMapping(value = CONFIG_LOADER_URL + "{id}", method = { RequestMethod.GET })
  public String viewConfig(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException  {
    logger.info("/configloader/{id} " + id);
    try {
      response.getWriter().println(service.generateHtmlResponse(id));
    } catch (TransformerException e) {
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    } catch (TagIdException e) {
      return ("redirect:" + "/configloader/errorform/" + id);
    }
    return null;
  }

  /**
   * Displays configuration for the given id.
   * @param id config id
   * @param response we write the html result to that HttpServletResponse response
   * @return nothing
   * @throws IOException
   * */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL + "{id}", method = { RequestMethod.GET })
  public String viewFinalReport(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException  {
    logger.info("/CONFIG_LOADER_PROGRESS_FINAL_REPORT_URL/{id} " + id);
    try {
      response.getWriter().println(service.getStoredConfigurationReportHtml(id));
    } catch (TransformerException e) {
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    } catch (TagIdException e) {
      return ("redirect:" + "/configloader/errorform/" + id);
    }
    return null;
  }  

  /**
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/configloader/errorform/{id}")
  public String viewConfigLoaderErrorForm(@PathVariable(value = "id") final String id, final Model model) {
    logger.info("/configloader/errorform " + id);

    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, id, CONFIG_LOADER_URL + id));
    model.addAttribute("err", id);
    return "errorFormWithData";
  }

  /**
   * Displays a form where a config id can be entered.
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewConfigLoaderWithForm(@PathVariable final String id, final Model model) {
    logger.info("/configloader/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, id, CONFIG_LOADER_URL + id));
    return "formWithData";
  }

  /**
   * Displays an input form for a config id, and if a POST was made with a config id, also the config data.
   * @param id config id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = CONFIG_LOADER_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewConfigLoaderFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/configloader/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR, CONFIG_LOADER_FORM_URL, null, null));
    else
      return ("redirect:" + CONFIG_LOADER_URL + id);
    return "formWithData";
  }

  /**
   * Displays an input form for a configuration id.
   * If a request is made from this form, a listener is registered that
   * listens for ProgressReport updates.
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL, method = RequestMethod.GET)
  public String startConfigurationProcessWithProgressReportForm(final Model model) {

    logger.info("CONFIG_LOADER_PROGRESS_REPORT_URL ");

    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR,
        CONFIG_LOADER_FORM_URL, "", CONFIG_LOADER_URL));
    model.addAttribute("reports", service.getFinalReports());
    return "configurationReportWithProgressReport";
  }  
  
  /**
   * Directly makes an ApplyConfiguration request for the specified id.
   * A listener is registered that listens for ProgressReport updates.
   * The progress update is shown in a progress bar.
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @param id The configuration id for which the request is made.
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/{id}", method = RequestMethod.GET)
  public String startConfigurationProcessWithProgressReportWithoutForm(
      @PathVariable(value = "id") final String id, final Model model) {

    logger.info("CONFIG_LOADER_PROGRESS_REPORT_URL/id:" + id);

    model.addAllAttributes(FormUtility.getFormModel(CONFIG_LOADER_FORM_TITLE, CONFIG_LOADER_FORM_INSTR,
        CONFIG_LOADER_FORM_URL, "", CONFIG_LOADER_URL));
    model.addAttribute("configurationId", id);
    return "configurationReportWithProgressReportWithoutForm";
  }  
  
  /**
   * Starts an applyConfiguration request to the server.
   * Listens for Progress Report updates.
   * @param configurationId the id of the configuration
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/start", method = RequestMethod.POST)
  public void startConfigurationProcess(@RequestParam("configurationId") final String configurationId) throws InterruptedException {
    logger.info("(AJAX) Starting Configuration Request: " + configurationId);
    service.getConfigurationReportWithReportUpdates(Integer.parseInt(configurationId));
  }

  /**
   * @param configurationId the id of the configuration
   * @return Returns the current progress of the request. 
   * @throws InterruptedException in case of error
   */
  @RequestMapping(value = CONFIG_LOADER_PROGRESS_REPORT_URL + "/getProgress", method = RequestMethod.POST)
  @ResponseBody
  public Integer getProgressReport(@RequestParam("configurationId") final String configurationId) throws InterruptedException {

    logger.info("(AJAX) Received Progress Report Request for configurationId:" + configurationId);

    ClientRequestProgressReport report = service.getReportForConfiguration(configurationId);
    if (report == null) {
      return 0;
    }

    int currentProgress = ((100 * report.getCurrentProgressPart()) / report.getTotalProgressParts());
    logger.info("returning:" + report.getCurrentProgressPart() + " out of " + report.getTotalProgressParts());

    // @ResponseBody will automatically convert the returned value into JSON format
    // You must have Jackson in your classpath
    return currentProgress;
  }
}
