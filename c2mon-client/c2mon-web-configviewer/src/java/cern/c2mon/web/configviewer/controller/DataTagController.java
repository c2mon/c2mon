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

import cern.c2mon.web.configviewer.service.TagIdException;
import cern.c2mon.web.configviewer.service.TagService;
import cern.c2mon.web.configviewer.util.FormUtility;

/**
 * A controller for the datatag viewer 
 * */
@Controller
public class DataTagController {


  private static final String TAGVIEWER_PAGE_NAME = "tagviewer";
  
  /**
   * A REST-style URL to tagviewer, combined with tag id displays datatag configuration
   * */
  private static final String TAG_URL = "../";

  /**
   * A URL to the tagviewer with input form
   * */
  public static final String TAG_FORM_URL = "/" + TAGVIEWER_PAGE_NAME + "/form";
  
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
  public static final String TAG_FORM_TITLE = "Tag  Viewer";

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
   * @return Redirects to the form
   * */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/", method = { RequestMethod.GET })
  public String viewTag(final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/");
    return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/form");
  }    

  /**
   * @return
   * Displays (TagConfig + TagValue) information for a tag with the specified id.
   * 
   * @param id tag id
   * @param response we write the html result to that HttpServletResponse response
   * */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/{id}", method = { RequestMethod.GET })
  public String viewTag(@PathVariable(value = "id") final String id, final HttpServletResponse response) throws IOException  {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/{id} " + id);
    try {
      response.setContentType("text/html; charset=UTF-8");
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
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    } 
    return null;
  }
  
  /**
   * @return 
   * Displays TagConfig information in RAW XML about a tag with the given id.
   * 
   * In case the TagId does not exist, redirects to an error form.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = TAG_CONFIG_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagConfigXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_CONFIG_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagConfigXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }
  
  /**
   * @return
   * Displays TagValue information in RAW XML about a tag with the given id.
   * 
   * In case the TagId does not exist, redirects to an error form.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = TAG_VALUE_XML_URL + "/{id}", method = { RequestMethod.GET })
  public String viewTagValueXml(@PathVariable final String id,  final Model model) {
    logger.info(TAG_VALUE_XML_URL + id);
    try {
      model.addAttribute("xml", service.getDataTagValueXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }
    return "raw_xml_views/rawXml";
  }

  /**
   * @return
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * 
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/errorform/{id}", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagErrorForm(@PathVariable(value = "id") final String errorId,
      @RequestParam(value = "id", required = false) final String id, final Model model) {
    
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/errorform " + id);
    
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, 
          TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
    else {
      return ("redirect:" + TAG_URL + id);
    }

    model.addAttribute("err", errorId);
    return "notFoundErrorFormWithData";
  }

  /**
   * Displays a form where a datatag id can be entered.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form/{id}", method = { RequestMethod.GET })
  public String viewTagWithForm(@PathVariable final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, id, TAG_URL + id));
    return "formWithData";
  }

  /**
   * Displays an input form for a datatag id, and if a POST was made with a datatag id, also the datatag information.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   * */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form " + id);
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
     else {
       return ("redirect:" + TAG_URL + TAGVIEWER_PAGE_NAME + "/" + id);
     }
      
    return "formWithData";
  }
}
