package cern.c2mon.web.configviewer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.validation.BindingResult;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.web.configviewer.util.LoginForm;

import java.util.Map;
import javax.validation.Valid;

@Controller
@RequestMapping("loginform.html")

public class LoginController {
  
        @RequestMapping(method = RequestMethod.GET)
        public String showForm(final Map model) {
                LoginForm loginForm = new LoginForm();
                model.put("loginForm", loginForm);
                return "loginform";
        }
//        
//        C2monServiceGateway.getSessionManager().isAuthorized

        @RequestMapping(method = RequestMethod.POST)
        public String processForm(@Valid LoginForm loginForm, final BindingResult result,
                  final Map model) {
                String userName = "UserName";
                String password = "password";
                if (result.hasErrors()) {
                        return "loginform";
                }
                loginForm = (LoginForm) model.get("loginForm");
                if (!loginForm.getUserName().equals(userName)
                                || !loginForm.getPassword().equals(password)) {
                        return "loginform";
                }
                model.put("loginForm", loginForm);
                return "loginsuccess";
        }
}