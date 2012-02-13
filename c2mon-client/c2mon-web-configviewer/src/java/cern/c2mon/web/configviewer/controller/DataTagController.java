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
   * URL to tagviewer, which displays tagconfig information
   * in RAW XML
   */
  public static final String TAG_CONFIG_XML_URL = "/tagconfig/xml";
  
  /**
   * URL to tagviewer, which displays tag value information
   * in RAW XML
   */
  public static final String TAG_VALUE_XML_URL = "/tagvalue/xml";
  
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
   * Displays configuration of a tag with the specified id
   * @param id tag id
   * @param response we write the html result to that HttpServletResponse response
   * @throws IOException 
   * */
  @RequestMapping(value = "/tagviewer/{id}", method = { RequestMethod.GET })
  public String viewTag(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException  {
    logger.info("/tagviewer/{id} " + id);
    try {
      response.getWriter().println(FormUtility.getHeader());
      response.getWriter().println(service.generateDataTagValueHtmlResponse(id));
      response.getWriter().println(service.generateDataTagConfigHtmlResponse(id));
      response.getWriter().println(FormUtility.getFooter());
      return null;
    } catch (TransformerException e) {
      logger.error(e.getMessage());
      response.getWriter().println(e.getMessage());
    } catch (TagIdException e) {
      logger.error(e.getMessage());
      return ("redirect:" + "/tagviewer/errorform/" + id);
    } 
    return null;
  }
  
  /**
   * Displays configuration of a process with the given id together with a form
   * @param id tag config id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = TAG_CONFIG_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagValueXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_CONFIG_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagConfigXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/tagviewer/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }
  
  /**
   * Displays configuration of a process with the given id together with a form
   * @param id tag value id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = TAG_VALUE_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagConfigXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_VALUE_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagValueXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/tagviewer/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }

  /**
   * Displays an input form for an tag id, and if a POST was made with an tag id, also the alarm data.
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/tagviewer/errorform/{id}")
  public String viewTagErrorForm(@PathVariable(value = "id") final String id, final Model model) {
    logger.info("/tagviewer/errorform " + id);

    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    model.addAttribute("err", id);
    return "errorFormWithData";
  }

  /**
   * Displays configuration of an alarm with the given id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/tagviewer/", method = { RequestMethod.GET })
  public String viewTag(final Model model) {
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
}
