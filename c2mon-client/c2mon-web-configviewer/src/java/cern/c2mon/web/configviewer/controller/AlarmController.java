package cern.c2mon.web.configviewer.controller;

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

import cern.c2mon.web.configviewer.service.AlarmService;
import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.util.FormUtility;

/**
 * A controller for the alarm viewer 
 * */

@Controller
public class AlarmController {

    /**
     * A REST-style URL to alarmviewer, combined with alarm id displays alarm configuration
     * */
    public static final String ALARM_URL = "/alarmviewer/";

    /**
     * A URL to the alarmviewer with input form
     * */
    public static final String ALARM_FORM_URL = "/alarmviewer/form";
    
    /**
     * Title for the alarm form page
     * */
    public static final String ALARM_FORM_TITLE = "Alarm Configuration Viewer";
    
    /**
     * Description for the alarm form page
     * */
    public static final String ALARM_FORM_INSTR = "Enter an alarm id to view the alarm's configuration.";
   
    /**
     * A link to the helpalarm
     * */
    public static final String HELPALARM_FORM = "http://oraweb.cern.ch/pls/timw3/helpalarm.AlarmForm?p_alarmid=";
    
    /**
     * An alarm service
     * */
    @Autowired
    private AlarmService service;
    
    /**
     * AlarmController logger
     * */
    private static Logger logger = Logger.getLogger(AlarmController.class);
    
    
    /**
     * Displays configuration of an alarm with the given id
     * @param id alarm id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = ALARM_URL + "{id}", method = { RequestMethod.GET })
    public String viewAlarm(@PathVariable final String id, final Model model) {
        logger.info("/alarmviewer/{id} " + id);
        model.addAllAttributes(getAlarmModel(id));
        return "tagInfo";
    }
    
    /**
     * Displays configuration of an alarm with the given id together with a form
     * @param id alarm id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/alarmviewer/form/{id}", method = { RequestMethod.GET })
    public String viewAlarmWithForm(@PathVariable final String id, final Model model) {
        logger.info("/alarmviewer/form/{id} " + id);
        model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, id, ALARM_URL + id));
        return "formWithData";
    }
    
    /**
     * Displays an input form for an alarm id, and if a POST was made with an alarm id, also the alarm data.
     * @param id alarm id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/alarmviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewAlarmFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
        logger.info("/alarmviewer/form " + id);
        if (id == null)
            model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, null, null));
        else
          return ("redirect:" + ALARM_URL + id);
          
        return "formWithData";
    }
    
    /**
     * A helper method to get the xml and build a map of values for the MVC model.
     * @param alarmId id of an alarm
     * @return a map of values to include in the model for a jsp page.
     * */
    private Map<String, String> getAlarmModel(final String alarmId) {
        Map<String, String> model = new HashMap<String, String>();
        try {
            String alarmXml = service.getAlarmTagXml(alarmId);
            model.put("alarmXml", alarmXml);
//            model.put("url", HELPALARM_FORM + alarmId);
//            model.put("urlText", "Display HelpAlarm information for this alarm");
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
