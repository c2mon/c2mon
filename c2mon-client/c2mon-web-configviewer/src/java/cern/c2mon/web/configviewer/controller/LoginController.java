package cern.c2mon.web.configviewer.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * A controller for the login screen. This is for link the url /login with the file loginNiceForm.jsp 
 **/
@Controller
@RequestMapping(value = "/login")
public class LoginController {
  
  @RequestMapping(method = RequestMethod.GET)
  public String login(final Model model, final HttpServletRequest request) {

      return "loginNiceForm";

  }
}
