package cern.c2mon.web.configviewer.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
  public static final String CONFIG_LOADER_XML_URL = "/configloader/xml";

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
   * Displays configuration of for the given id.
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
}
