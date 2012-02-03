package cern.c2mon.web.configviewer.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.web.configviewer.service.HistoryService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;


/**
 * A controller for the command viewer 
 * */
@Controller
public class HistoryController {

    /**
     * A REST-style URL to commandviewer, combined with command id displays command configuration
     * */
    public static final String HISTORY_URL = "/historyviewer/";
    
    /**
     * A URL to the commandviewer with input form
     * */
    public static final String HISTORY_FORM_URL = "/historyviewer/form";
    
    /**
     * Title for the command form page
     * */
    public static final String HISTORY_FORM_TITLE = "History Viewer";
    
    /**
     * Description for the command form page
     * */
    public static final String HISTORY_FORM_INSTR = "Enter a tag id to view the History.";
    
    /**
     * A command service
     * */
    @Autowired
    private HistoryService service;

    /**
     * CommandController logger
     * */
    private static Logger logger = Logger.getLogger(HistoryController.class);
    
    /**
     * Displays configuration of an alarm with the given id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = HISTORY_URL, method = { RequestMethod.GET })
    public String viewHistory(final Model model) {
      logger.info(HISTORY_URL);
      return ("redirect:" + HISTORY_FORM_URL);
    }    
    
    /**
     * @param id tag id
     * @param response we write the html result to that HttpServletResponse response
     * @return nothing
     * */
    @RequestMapping(value = "/historyviewer/{id}", method = { RequestMethod.GET })
    public String viewHistory(@PathVariable(value = "id") final String id, final HttpServletResponse response)  {
      logger.info("/historyviewer/{id} " + id);
      
      int numberOfHistoryRecords = 100; // we ask for that many history records
      
      try {
        response.getWriter().println(service.generateHtmlResponse(id, numberOfHistoryRecords));
      } catch (IOException e) {
        e.printStackTrace();
        logger.error(e.getMessage());
      } catch (TagIdException e) {
        return ("redirect:" + "/historyviewer/errorform/" + id);
      }
      return null;
    }
    
    /**
     * Displays an input form for an alarm id, and if a POST was made with an alarm id, also the alarm data.
     * @param id alarm id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/historyviewer/errorform/{id}")
    public String viewCommandErrorForm(@PathVariable(value = "id") final String id, final Model model) {
        logger.info("/historyviewer/errorform " + id);
        
        model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
        model.addAttribute("err", id);
       return "errorFormWithData";
    }
    
    /**
     * Displays configuration of a command with the given id together with a form
     * @param id command id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/historyviewer/form/{id}", method = { RequestMethod.GET })
    public String viewCommandWithForm(@PathVariable final String id, final Model model) {
        logger.info("/historyviewer/form/{id} " + id);
        model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, id, HISTORY_URL + id));
        return "formWithData";
    }
    
    /**
     * Displays an input form for a command id, and if a POST was made with a command id, also the command data.
     * @param id command id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/historyviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewCommandFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
        logger.info("/historyviewer/form " + id);
        if (id == null)
            model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, null, null));
        else
           return ("redirect:" + HISTORY_URL + id);
        return "formWithData";
    }
    
    /**
     * A helper method to get the xml and build a map of values for the MVC model.
     * @param commandId id of a command
     * @return a map of values to include in the model for a jsp page.
     * */
    private Map<String, String> getHistoryModel(final String commandId) {
        Map<String, String> model = new HashMap<String, String>();
        try {
            String commandXml = service.getHistoryXml(commandId,100);
            model.put("commandXml", commandXml);
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
