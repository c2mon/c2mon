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

import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.service.TagService;
import cern.c2mon.web.configviewer.util.FormUtility;

/**
 * A controller for the datatag viewer 
 * */
@Controller
public class DataTagController {

    /**
     * A link to the helpalarm
     * */
    public static final String HELPALARM_LIST = "https://oraweb.cern.ch/pls/timw3/helpalarm.AlarmList?p_pointid1=";

    /**
     * A REST-style URL to tagviewer, combined with tag id displays datatag configuration
     * */
    public static final String TAG_URL = "/tagviewer/";
    
    /**
     * A URL to the tagviewer with input form
     * */
    public static final String TAG_FORM_URL = "/tagviewer/form";
    
    /**
     * Title for the datatag form page
     * */
    public static final String TAG_FORM_TITLE = "DataTag Configuration Viewer";
    
    /**
     * Description for the datatag form page
     * */
    public static final String TAG_FORM_INSTR = "Enter a tag id to view the tag's configuration.";
    
    /**
     * A datatag service
     * */
    @Autowired
    private TagService service;

    /**
     * DataTagController logger
     * */
    private static Logger logger = Logger.getLogger(DataTagController.class);

    /**
     * Displays configuration of a datatag with the given id
     * @param id datatag id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/tagviewer/{id}", method = { RequestMethod.GET })
    public String viewTag(@PathVariable final String id, final Model model) {
        logger.info("/tagviewer/{id} " + id);
        model.addAllAttributes(getDataTagModel(id));
        return "tagInfo";
    }
    
    /**
     * Displays configuration of an alarm with the given id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/tagviewer/", method = { RequestMethod.GET })
    public String viewAlarm(final Model model) {
      logger.info("/tagviewer/");
      return ("redirect:" + "/tagviewer/form");
    }    
    
    /**
     * Displays configuration of a datatag with the given id together with a form
     * @param id datatag id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/tagviewer/form/{id}", method = { RequestMethod.GET })
    public String viewTagWithForm(@PathVariable final String id, final Model model) {
        logger.info("/tagviewer/form/{id} " + id);
        model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, id, TAG_URL + id));
        return "formWithData";
    }
    
    /**
     * Displays an input form for a datatag id, and if a POST was made with a datatag id, also the datatag configuration.
     * @param id datatag id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/tagviewer/form", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
        logger.info("/tagviewer/form " + id);
        if (id == null)
            model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, null));
        else
          return ("redirect:" + TAG_URL + id);
          
        return "formWithData";
    }
    
    /**
     * A helper method to get the xml and build a map of values for the MVC model.
     * @param tagId id of a datatag
     * @return a map of values to include in the model for a jsp page.
     * */
    private Map<String, String> getDataTagModel(final String tagId) {
        Map<String, String> model = new HashMap<String, String>();
        try {
            String dtXml = service.getDataTagValueXml(tagId);
            model.put("dataTagXml", dtXml);
            String cXml = service.getDataTagConfigXml(tagId);
            model.put("configXml", cXml);
            model.put("url", HELPALARM_LIST + tagId);
            model.put("urlText", "Display additional information from HelpAlarm for this DataTag");

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
