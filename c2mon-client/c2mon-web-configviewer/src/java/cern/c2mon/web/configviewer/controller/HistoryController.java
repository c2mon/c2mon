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

import cern.c2mon.client.common.history.exception.HistoryProviderException;
import cern.c2mon.client.common.history.exception.LoadingParameterException;
import cern.c2mon.web.configviewer.service.HistoryService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the history viewer 
 * */
@Controller
public class HistoryController {

  /**
   * Base URL for the history viewer
   * */
  public static final String HISTORY_URL = "/historyviewer/";

  /**
   * A URL to the history viewer with input form
   * */
  public static final String HISTORY_FORM_URL = "/historyviewer/form";
  
  /**
   * The URL to view the history of a tag in RAW XML format
   */
  public static final String HISTORY_XML_URL = HISTORY_URL + "xml";

  /**
   * Title for the history form page
   * */
  public static final String HISTORY_FORM_TITLE = "History Viewer";

  /**
   * Instruction for the history form page
   * */
  public static final String HISTORY_FORM_INSTR = "Enter a tag id to view the last 100 records in History.";

  /** How many records in history to ask for. 100 looks ok! */
  private static final int HISTORY_RECORDS_TO_ASK_FOR = 100;
  
  /**
   * A history service
   * */
  @Autowired
  private HistoryService service;

  /**
   * HistoryController logger
   * */
  private static Logger logger = Logger.getLogger(HistoryController.class);

  /**
   * Displays a form where a tag id can be entered.
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = HISTORY_URL, method = { RequestMethod.GET })
  public String viewHistory(final Model model) {
    logger.info(HISTORY_URL);
    return ("redirect:" + HISTORY_FORM_URL);
  }    

  /**
   * Displays the history of a given id.
   * @param id the last 100 records of the given tag id are being shown
   * @param response the html result is written to that HttpServletResponse response
   * @return nothing
   * @throws IOException 
   * */
  @RequestMapping(value = "/historyviewer/{id}", method = { RequestMethod.GET })
  public String viewHistory(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException  {
    logger.info("/historyviewer/{id} " + id);

    try {
      response.getWriter().println(service.generateHtmlResponse(id, HISTORY_RECORDS_TO_ASK_FOR));
    } catch (TagIdException e) {
      return ("redirect:" + "/historyviewer/errorform/" + id);
    } catch (TransformerException e) {
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    }
    catch (Exception e) {
      response.getWriter().println(e.getMessage());
      logger.error(e.getMessage());
    }
    return null;
  }
  
  /**
   * Displays the History in RAW XML for a tag with the given id.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = HISTORY_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    logger.info(HISTORY_XML_URL + id);
    try {
      model.addAttribute("xml", service.getHistoryXml(id, HISTORY_RECORDS_TO_ASK_FOR));
    } catch (HistoryProviderException e) {
      logger.error(e.getMessage());
      return ("redirect:" + "/historyviewer/errorform/" + id);
    } catch (LoadingParameterException e) {
      logger.error(e.getMessage());
      return ("redirect:" + "/historyviewer/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }

  /**
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/historyviewer/errorform/{id}")
  public String viewHistoryErrorForm(@PathVariable(value = "id") final String id, final Model model) {
    logger.info("/historyviewer/errorform " + id);

    model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
    model.addAttribute("err", id);
    return "errorFormWithData";
  }

  /**
   * Displays a form where a tag id can be entered.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/historyviewer/form/{id}", method = { RequestMethod.GET })
  public String viewHistoryWithForm(@PathVariable final String id, final Model model) {
    logger.info("/historyviewer/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
    return "formWithData";
  }

  /**
   * Displays an input form for a tag id, and if a POST was made with a tag id, also the history data.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/historyviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewHistoryFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/historyviewer/form " + id);
    if (id == null)
      model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, null, null));
    else
      return ("redirect:" + HISTORY_URL + id);
    return "formWithData";
  }
}
