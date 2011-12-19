package cern.c2mon.web.configviewer.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cern.c2mon.web.configviewer.util.LoginForm;

@Controller
@RequestMapping("timeout.html")

public class TimeOutController {

  @RequestMapping(method = RequestMethod.GET)
  public String showForm(final Map model) {
    
    model.put("timeout", "");
    
    return "timeout";
  }
}
