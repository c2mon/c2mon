package cern.c2mon.web.configviewer.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A controller for the access denied screen. 
 **/
@Controller
@RequestMapping(value = "/accessDenied")
public class AccessDenied {

  @RequestMapping(method = RequestMethod.GET)
  public String accessDenied(final Model model, final HttpServletRequest request) {

    return "accessDenied";
  } 
}
