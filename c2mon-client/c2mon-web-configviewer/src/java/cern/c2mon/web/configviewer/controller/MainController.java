package cern.c2mon.web.configviewer.controller;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping(value="/")
public class MainController {
    
    @RequestMapping(method=RequestMethod.GET)
    public String getCreateForm(Model model, HttpServletRequest request) {
      
        String username = "";

        if (request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }

        model.addAttribute("username", username);
        return "home";
    }
}
