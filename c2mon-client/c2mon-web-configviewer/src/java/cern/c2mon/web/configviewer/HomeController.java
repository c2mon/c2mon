package cern.c2mon.web.configviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.web.configviewer.model.DataTagInfo;
import cern.c2mon.web.configviewer.service.DataTagService;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {

    public static String HELPALARM_LIST = "https://oraweb.cern.ch/pls/timw3_oper/helpalarm.AlarmList?p_pointid1=";
    public static String HELPALARM_FORM = "http://oraweb.cern.ch/pls/timw3_oper/helpalarm.AlarmForm?p_alarmid=";
    
    @Autowired
    DataTagService dtService;

    @RequestMapping(value = "/")
    public String home() {
        System.out.println("HomeController: Passing through...");
        return "home";
    }

    @RequestMapping(value = "/tagviewer")
    public String tagInput(@RequestParam(value = "tagId", required = false) String tagId, Model model) {
        System.out.println("Tag viewer for tag " + tagId);
        model.addAttribute("instruction", "Enter a tag id to view the tag's configuration.");
        model.addAttribute("objectName", "tagId");
        model.addAttribute("objectValue", tagId);
        if (tagId == null) {
            return "dataTag";
        }
        DataTagInfo info = dtService.getDataTagInfo(tagId);
        if (info.isEmpty())
            model.addAttribute("err", "No data tag found");
        model.addAttribute("name", info.getName());
        model.addAttribute("id", info.getId());
        model.addAttribute("value", info.getValue());
        model.addAttribute("config", info.getConfig());
        model.addAttribute("address", info.getAddress());
        model.addAttribute("url", HELPALARM_LIST + info.getId());
        model.addAttribute("urlText", "Display additional information from HelpAlarm for this DataTag");
        return "dataTag";
    }
    
    @RequestMapping(value = "/alarmviewer")
    public String alarmInput(@RequestParam(value = "alarmId", required = false) String alarmId, Model model) {
        System.out.println("Alarm viewer");
        //Alarm Configuration Viewer
        model.addAttribute("instruction", "Enter an alarm id to view the alarm's configuration.");
        model.addAttribute("objectName", "alarmId");
        model.addAttribute("objectValue", alarmId);
        
        DataTagInfo info = dtService.getAlarmInfo(alarmId);
        if (info.isEmpty())
            model.addAttribute("err", "No alarm found");
        model.addAttribute("name","faultFamily:FaultMember:getFaultCode");
        model.addAttribute("value", info.getValue());
        model.addAttribute("config", info.getConfig());
        model.addAttribute("url", HELPALARM_FORM + alarmId);
        model.addAttribute("urlText", "Display HelpAlarm information for this alarm");
        
        return "dataTag";
    }

/*
    @RequestMapping(value = "/commandviewer")
    public String commandInput(@RequestParam(value = "commandId", required = false) String commandId, Model model) {
        System.out.println("Command viewer");
        model.addAttribute("instruction", "Enter a command id to view the command's configuration.");
        model.addAttribute("objectName", "commandId");
        model.addAttribute("objectValue", commandId);
        return "inputCommand";
    }*/

    
    @RequestMapping(value = "/tagview/{tagId}")
    public String tagViewer(@PathVariable String tagId, Model model) {
        System.out.println("Tag viewer for tag " + tagId);
        model.addAttribute("instruction", "Enter a tag id to view the tag's configuration.");
        model.addAttribute("objectName", "tagId");
        model.addAttribute("objectValue", tagId);
        if (tagId == null) {
            return "dataTag";
        }

        DataTagInfo info = dtService.getDataTagInfo(tagId);
        if (info.isEmpty())
            model.addAttribute("err", "No data tag found");

        model.addAttribute("name", info.getName());
        model.addAttribute("id", info.getId());
        model.addAttribute("value", info.getValue());
        model.addAttribute("config", info.getConfig());
        model.addAttribute("address", info.getAddress());

        return "dataTag";
    }

}

// <li><a href="http://timweb.cern.ch:8888/tim/tagviewer" target="portalIframe">
// Datatag Viewer </a></li>
//
// <li><a href="http://timweb.cern.ch:8888/tim/alarmviewer"
// target="portalIframe">
// Alarm Viewer </a></li>
//
// <li><a href="http://timweb.cern.ch:8888/tim/commandviewer"
// target="portalIframe">
// Command Viewer </a></li>
//
// <li><a href="http://timweb.cern.ch:8888/tim/configviewer"
// target="portalIframe">
// Process Config Viewer </a></li>