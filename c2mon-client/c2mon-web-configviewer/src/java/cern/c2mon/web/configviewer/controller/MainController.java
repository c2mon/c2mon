package cern.c2mon.web.configviewer.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Handles requests for the application home page.
 * 
 * This is a super simple html page 
 * (we also add the username of the user to show a personalised message once he is logged in).
 */
@Controller
@RequestMapping(value = "/")
public class MainController {

  /**
   * This is the application home page.
   * 
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @param request Used to get the username
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(method = RequestMethod.GET)
  public String getCreateForm(final Model model, final HttpServletRequest request) {

    String username = "";

    if (request.getUserPrincipal() != null) {
      username = request.getUserPrincipal().getName();
    }

    model.addAttribute("username", username);
    return "home";
  }
}
