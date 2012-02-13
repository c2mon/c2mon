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
 * A controller for the command viewer 
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
   * CommandController logger
   * */
  private static Logger logger = Logger.getLogger(ConfigLoaderController.class);

  /**
   * Displays configuration of an alarm with the given id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = CONFIG_LOADER_URL, method = { RequestMethod.GET })
  public String viewConfig(final Model model) {
    logger.info(CONFIG_LOADER_URL);
    return ("redirect:" + "/configloader/form");
  }    

  /**
   * Displays configuration of a process with the given process name
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
   * Displays an input form for an alarm id, and if a POST was made with an alarm id, also the alarm data.
   * @param id alarm id
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
   * Displays configuration of a command with the given id together with a form
   * @param id command id
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
   * Displays an input form for a command id, and if a POST was made with a command id, also the command data.
   * @param id command id
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
