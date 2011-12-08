package cern.c2mon.web.configviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.validation.BindingResult;

import cern.c2mon.client.auth.AuthorizationManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.web.configviewer.util.LoginForm;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.shared.common.command.AuthorizationDetails;

import java.util.Map;
import javax.validation.Valid;

@Controller
@RequestMapping("loginform.html")

public class LoginController {
  
  private final static String APP_NAME = "c2mon-web-configviewer:Login-Controller";

  @RequestMapping(method = RequestMethod.GET)
  public String showForm(final Map model) {
    LoginForm loginForm = new LoginForm();
    model.put("loginForm", loginForm);
    return "loginform";
  }
  
  @RequestMapping(method = RequestMethod.POST)
  public String processForm(@Valid LoginForm loginForm, final BindingResult result,
      final Map model) {
    
    if (result.hasErrors()) {
      return "loginform";
    }

    loginForm = (LoginForm) model.get("loginForm");
    
    String username = loginForm.getUserName();
    String password = loginForm.getPassword();
    
//    if (!C2monServiceGateway.getSessionManager().login(APP_NAME, username, password)) {
//      return "loginform";
//    }
    
    model.put("loginForm", loginForm);
    return "loginsuccess";
  }
}