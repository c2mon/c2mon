package cern.c2mon.web.configviewer.controller;

import java.io.IOException;

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

import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
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
  
  /** URL to define Last Records */
  public static final String LAST_RECORDS_URL = "/records/";

  /**
   * A URL to the history viewer with input form
   * */
  public static final String HISTORY_FORM_URL = "/historyviewer/form";
  
  /**
   * The URL to view the history of a tag in RAW XML format
   */
  public static final String HISTORY_XML_URL = HISTORY_URL + "xml";
  
  /**
   * The URL to view the history of a tag in CSV format
   */
  public static final String HISTORY_CSV_URL = HISTORY_URL + "csv";

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
   * @return Redirects to the form
   */
  @RequestMapping(value = HISTORY_URL, method = { RequestMethod.GET })
  public String viewHistory(final Model model) {
    logger.info(HISTORY_URL);
    return ("redirect:" + HISTORY_FORM_URL);
  }    

  /**
   * @return Displays the history of a given id.
   * 
   * @param id the last 100 records of the given tag id are being shown
   * @param response the html result is written to that HttpServletResponse response
   * */
  @RequestMapping(value = "/historyviewer/{id}", method = { RequestMethod.GET })
  public String viewHistory(
      @PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException {
    
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
   * @return 
   * Displays the history of a given id, for a specified by the user number of records.
   * 
   * @param id History of this Id is shown.
   * @param records User can specify how many records to go back in history.
   * @param response the html result is written to that HttpServletResponse response
   * */
  @RequestMapping(value = "/historyviewer/{id}/records/{records}", method = { RequestMethod.GET })
  public String viewHistory(@PathVariable(value = "id") final String id,
        @PathVariable(value = "records") final int lastRecords,
          final HttpServletResponse response) throws IOException  {
    
    logger.info("/historyviewer/{id}/records/{records}" + id + " ," + lastRecords);

    try {
      response.getWriter().println(service.generateHtmlResponse(id, lastRecords));
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
   * @return Displays the History in RAW XML for a tag with the given id.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
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
   * @return In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = "/historyviewer/errorform/{id}")
  public String viewHistoryErrorForm(@PathVariable(value = "id") final String id, final Model model) {
    logger.info("/historyviewer/errorform " + id);

    model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
    model.addAttribute("err", id);
    return "notFoundErrorFormWithData";
  }

  /**
   * @return Displays a form where a tag id can be entered.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = "/historyviewer/form/{id}", method = { RequestMethod.GET })
  public String viewHistoryWithForm(@PathVariable final String id, final Model model) {
    logger.info("/historyviewer/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
    return "formWithData";
  }

  /**
   * @return Displays an input form for a tag id, 
   * and if a POST was made with a tag id, it redirects to HISTORY_URL + id + LAST_RECORDS_URL + records
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = "/historyviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewHistoryFormPost(@RequestParam(value = "id", required = false) final String id,
      @RequestParam(value = "records", required = false) final String records,
      final Model model) {
    logger.info("/historyviewer/form " + id);
    if (id == null) {
      model.addAllAttributes(FormUtility
          .getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, null, null));
    }
    else {
      return ("redirect:" + HISTORY_URL + id + LAST_RECORDS_URL + records);
    }
    return "trend_views/trend_view_form";
  }
}
