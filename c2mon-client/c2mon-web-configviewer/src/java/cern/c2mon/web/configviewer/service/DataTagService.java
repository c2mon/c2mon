package cern.c2mon.web.configviewer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientCommandTag;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.core.C2monCommandManager;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.C2monTagManager;
import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.web.configviewer.model.AlarmInfo;
import cern.c2mon.web.configviewer.model.DataTagInfo;
import cern.c2mon.web.configviewer.model.KeyValue;

@Service
public class DataTagService {

    private C2monTagManager tagManager;
    private C2monCommandManager commandManager;

    {
        startC2monServiceGateway();
        tagManager = C2monServiceGateway.getTagManager();
        commandManager = C2monServiceGateway.getCommandManager();
    }

    private void startC2monServiceGateway() {
        C2monServiceGateway.startC2monClient();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getCommandInfo(String commandId) {
        ClientCommandTag com = getCommandTag(Long.parseLong(commandId));
        com.getName();
        com.getDescription();
        com.getMinValue();
        com.getMaxValue();
        com.getType();
        com.getValue();
    }
    
    public DataTagInfo getAlarmInfo(String alarmId) {
        DataTagInfo info = new DataTagInfo();
        List<KeyValue> value = new ArrayList<KeyValue>();
        value.add(new KeyValue("State","?"));
        value.add(new KeyValue("Timestamp of last change","?"));
        value.add(new KeyValue("Info","?"));
        List<KeyValue> config = new ArrayList<KeyValue>();
        config.add(new KeyValue("Alarm id", "?"));
        config.add(new KeyValue("Fault Family", "?"));
        config.add(new KeyValue("Fault member", "?"));
        config.add(new KeyValue("Fault code", "?"));
        config.add(new KeyValue("Problem description", "?"));
        config.add(new KeyValue("DataTag", "?")); 
//        str.append("<a href=\"tagviewer?tagid=");
//        str.append(alarm.getDataTagId());
//        str.append("\">");
//        str.append(tag.getName());
//        str.append("</a>");
        config.add(new KeyValue("Activation condition", "?"));
     
        info.setValue(value);
        info.setConfig(config);
        
        return info;
    }

    public DataTagInfo getDataTagInfo(String dataTagId) {
        ClientDataTagValue tag = getDataTag(Long.parseLong(dataTagId));
        DataTagInfo info = new DataTagInfo();
        info.setId(dataTagId);
        if (tag == null)
            return info;
        info.setName(tag.getName());

        List<KeyValue> value = new ArrayList<KeyValue>();
        value.add(new KeyValue("Value", tag.getValue().toString()));
        value.add(new KeyValue("Value description","?"));// tag.getValueDescription()));
        value.add(new KeyValue("Source timestamp", tag.getTimestamp().toString()));
        value.add(new KeyValue("Server timestamp", tag.getServerTimestamp().toString()));
        value.add(new KeyValue("Quality code", "?"));
        value.add(new KeyValue("Quality description", tag.getDataTagQuality().getDescription()));
        value.add(new KeyValue("Simulated", Boolean.toString(tag.isSimulated())));
        info.setValue(value);

        List<KeyValue> config = new ArrayList<KeyValue>();

        config.add(new KeyValue("Tag id", Long.toString(tag.getId())));
        config.add(new KeyValue("Tag name", tag.getName()));
        config.add(new KeyValue("Description", tag.getDescription()));

        config.add(new KeyValue("Control tag", "?" )); //tag.isControlTag().toString()));
        config.add(new KeyValue("Unit", tag.getUnit()));
        config.add(new KeyValue("Mode", tag.getMode().toString()));

        config.add(new KeyValue("Min value", "?"));//tag.getMinValue()));
        config.add(new KeyValue("Max value", "?"));//tag.getMaxValue()));
        config.add(new KeyValue("JMS Topic name", "?"));//tag.getTopicName()));
        config.add(new KeyValue("Data type", "?"));//tag.getType().getName()));
        config.add(new KeyValue("Value dictionary", "?"));

        // alarms
        Collection<Long> alarmIds = tag.getAlarmIds();
        if (alarmIds == null || alarmIds.isEmpty()) {
            config.add(new KeyValue("Alarms", "No alarm defined for this tag"));
        } else {
            StringBuilder alarmList = new StringBuilder();
            for (long alarmId : alarmIds) {
                alarmList.append("<a href=\"alarmviewer?alarmid=" + alarmId + "\">" + alarmId + "</a><br/>");
            }
            config.add(new KeyValue("Alarms", alarmList.toString()));
        }
        // rules
        Collection<Long> ruleIds = tag.getRuleIds();
        if (ruleIds == null || ruleIds.isEmpty()) {
            config.add(new KeyValue("Rules", "No rules defined for this tag"));
        } else {
            StringBuffer ruleList = new StringBuffer();
            for (long ruleId : ruleIds) {
                ruleList.append("<a href=\"tagviewer?tagid=" + ruleId + "\">" + ruleId + "</a><br/>");
            }
            config.add(new KeyValue("Rules", ruleList.toString()));
        }

        if (tag.isRuleResult()) {
            String ruleText = tag.getRuleExpression().getExpression();
            ruleText = ruleText.replaceAll(",", ",</br>");

            Set<Long> inputTagIds = tag.getRuleExpression().getInputTagIds();
            if (!inputTagIds.isEmpty()) {
                for (long id : inputTagIds) {
                    ruleText = ruleText.replaceAll("#" + id, "<a href=\"tagviewer?tagid=" + id + "\">#" + id + "</a>");
                }
            }
            config.add(new KeyValue("Rule Text", ruleText));
        }

        config.add(new KeyValue("DIP Publication", tag.getDipPublication()));
        config.add(new KeyValue("JAPC Publication", tag.getJapcPublication()));

        // TODO: add missing properties + add all hardware information

        List<KeyValue> address = new ArrayList<KeyValue>();
        
        address.add(new KeyValue("JMS Priority",  "?"));//Integer.toString(tag.getPriority()) ) );
        address.add(new KeyValue("Time deadband (ms)",  "?"));//Integer.toString(tag.getTimeDeadband()) ));
        
        address.add(new KeyValue("Value deadband", "?"));//Float.toString(tag.getValueDeadband()) ));
        address.add(new KeyValue("Value deadband type",  "?"));//Short.toString(tag.getValueDeadbandType()) ));
        address.add(new KeyValue("Hardware Address",  "?"));//tag.getHardwareAddress()));
                
        info.setValue(value);
        info.setConfig(config);
        info.setAddress(address);
        
        return info;
    }

    private ClientCommandTag getCommandTag(long commandId) {
        ClientCommandTag ct;
        System.out.println("CommandManager is " + (commandManager == null ? "null" : "not null"));
        List<Long> commandIds = new ArrayList<Long>();
        commandIds.add(commandId);
        Collection<ClientCommandTag> commandTags = commandManager.getCommandTags(commandIds);
        Iterator<ClientCommandTag> it = commandTags.iterator();
        if (it.hasNext())
            ct = it.next();
        else
            ct = null;
        System.out.println("CommandTag id = " + (ct == null ? "null" : ct.getId()));
        return ct;
    }

    private ClientDataTagValue getDataTag(long dataTagId) {
        ClientDataTagValue dt;
        System.out.println("TagManager is " + (tagManager == null ? "null" : "not null"));

        List<Long> tagIds = new ArrayList<Long>();
        tagIds.add(dataTagId);
        Collection<ClientDataTagValue> dataTags = tagManager.getDataTags(tagIds);

        Iterator<ClientDataTagValue> it = dataTags.iterator();
        if (it.hasNext()) {
            dt = it.next();
        } else {
            dt = null;
        }
        System.out.println("Datatag id = " + (dt == null ? "null" : dt.getId()));
        return dt;
    }
}
