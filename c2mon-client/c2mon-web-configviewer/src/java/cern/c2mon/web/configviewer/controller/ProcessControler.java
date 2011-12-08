package cern.c2mon.web.configviewer.controller;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import cern.c2mon.web.configviewer.service.CommandService;
import cern.c2mon.web.configviewer.service.ConfigLoaderService;
import cern.c2mon.web.configviewer.service.ProcessService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.service.TagService;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the command viewer 
 * */
@Controller
public class ProcessControler {

  /**
   * A REST-style URL 
   * */
  public static final String PROCESS_URL = "/process/";
  
  /**
   * A URL to the process viewer with input form
   * */
  public static final String PROCESS_XML_URL = PROCESS_URL + "xml";

  /**
   * A URL to the process viewer with input form
   * */
  public static final String PROCESS_FORM_URL = PROCESS_URL + "form";

  /**
   * Title for the process form page
   * */
  public static final String PROCESS_FORM_TITLE = "Process";

  /**
   * Description for the process form page
   * */
  public static final String PROCESS_FORM_INSTR = "Select process Name.";

  /**
   * A process loader service
   * */
  @Autowired
  private ProcessService service;

  /**
   * CommandController logger
   * */
  private static Logger logger = Logger.getLogger(ProcessControler.class);


  /**
   * Displays configuration of a command with the given id
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = PROCESS_URL + "{id}", method = { RequestMethod.GET })
  public String viewCommand(@PathVariable final String id, final Model model) {
    logger.info("/process/{id} " + id);
    model.addAllAttributes(getProcessModel(id));
    return "tagInfo";
  }

  /**
   * Displays configuration of a process with the given id together with a form
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = PROCESS_FORM_URL + "/{id}", method = { RequestMethod.GET })
  public String viewCommandWithForm(@PathVariable final String id,  final Model model) {
    logger.info("/process/form/{id} " + id);
    model.addAllAttributes(getProcessFormModel(PROCESS_FORM_TITLE, PROCESS_FORM_INSTR, PROCESS_FORM_URL, id, PROCESS_URL + id));
    return "processFormWithData";
  }
  
  /**
   * Displays configuration of a process with the given id together with a form
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = PROCESS_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    logger.info("/process/xml/{id} " + id);
    model.addAllAttributes(getProcessModel(id));
    return "processXml";
  }

  /**
   * Displays an input form, and if a POST was made, also the process xml data.
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = PROCESS_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public String viewCommandFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/process/form " + id);
    if (id == null)
      model.addAllAttributes(getProcessFormModel(PROCESS_FORM_TITLE, PROCESS_FORM_INSTR, PROCESS_FORM_URL, null, null));
    else
      model.addAllAttributes(getProcessFormModel(PROCESS_FORM_TITLE, PROCESS_FORM_INSTR, PROCESS_FORM_URL, id, PROCESS_URL + id));

    return "processFormWithData";
  }


  /**
   * Gets a map of values to include later in the MVC model processed by a jsp.
   * This form is used for the process form and
   * includes a collection of all available process names in addition to the other forms.
   * @param title tag title of the form displayed on the jsp page (different for datatag, alarm, command)
   * @param instruction description of the user action displayed on the jsp page
   * @param formSubmitUrl url to which the form should be submitted
   * @param formTagValue previous value of a tag (datatag, alarm, command) entered in the form, that should be displayed in the form
   * @param processNames a collection of all available process names
   * @return a map of values ready to be used in the MVC model 
   * */
  public Map<String, Object> getProcessFormModel(final String title, final String instruction
      , final String formSubmitUrl, final String formTagValue, final String tagDataUrl) {

    Map<String, Object> model = new HashMap<String, Object>();
    model.put("title", title);
    model.put("instruction", instruction);
    model.put("formSubmitUrl", formSubmitUrl);
    model.put("formTagValue", formTagValue);
    model.put("tagDataUrl", tagDataUrl);

    try {
      Collection<String> names = service.getProcessNames();
      model.put("processNames", names);

    } catch (Exception e) {
      model.put("tagErr", "Unexpected problem occured. Try again or contact C2MON support");
      logger.error("Unexpected problem occured while getting the XML:", e);
    }

    return model;
  }

  /**
   * A helper method to get the xml and build a map of values for the MVC model.
   * @param processName the process name
   * @return a map of values to include in the model for a jsp page.
   * */
  private Map<String, Object>  getProcessModel(final String processName) {

    Map<String, Object> model = new HashMap<String, Object>();

    try {
      String processXml = service.getProcessXml(processName);
      Collection<String> names = service.getProcessNames();

      model.put("processXml", processXml);
      model.put("processNames", names);

    } catch (TagIdException e) {
      model.put("tagErr", e.getMessage());
      logger.error(e);
    } catch (Exception e) {
      model.put("tagErr", "Unexpected problem occured. Try again or contact C2MON support");
      logger.error("Unexpected problem occured while getting the XML:", e);
    }
    return model;
  }
}
