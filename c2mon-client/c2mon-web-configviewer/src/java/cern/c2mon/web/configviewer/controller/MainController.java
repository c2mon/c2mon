package cern.c2mon.web.configviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Handles requests for the application home page.
 */
@Controller
public class MainController {
    
    /**
     * Home page. It may be interesting to provide here the links to the specific pages of tagviewer, alarmviewer, etc.
     * @return name of a jsp page which will be displayed
     * */
    @RequestMapping(value = "/")
    public String home() {
        return "home";
    }

}
