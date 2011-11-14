package cern.c2mon.web.configviewer.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;

/**
 * Alarm service providing the XML representation of a given alarm
 * */
@Service
public class AlarmService {

    /**
     * AlarmService logger
     * */
    private static Logger logger = Logger.getLogger(AlarmService.class);
    
    /**
     * Gateway to C2monService 
     * */
    @Autowired
    private ServiceGateway gateway;
    
    /**
     * Gets the XML representation of the current value and configuration of an alarm
     * @param alarmId id of the alarm
     * @return XML representation of alarm value and configuration 
     * @throws Exception if alarm was not found or a non-numeric id was requested ({@link TagIdException}), or any other exception
     * thrown by the underlying service gateway.
     * */
    public String getAlarmTagXml(final String alarmId) throws Exception {
        try { 
            AlarmValueImpl alarm = (AlarmValueImpl) getAlarmValue(Long.parseLong(alarmId));
            if (alarm != null)
                return alarm.getXml();
            else
                throw new TagIdException("No alarm found");
        } catch (NumberFormatException e) {
            throw new TagIdException("Invalid alarm id");
        }
    }
    
    /**
     * Retrieves a alarmValue object from the service gateway tagManager
     * @param alarmId id of the alarm
     * @return alarm value
     * */
    private AlarmValue getAlarmValue(final long alarmId) {
        AlarmValue av = null;
        List<Long> alarmIds = new ArrayList<Long>();
        alarmIds.add(alarmId);
        Collection<AlarmValue> alarms = gateway.getTagManager().getAlarms(alarmIds); 
            //tagManager.getAlarms(alarmIds);
        Iterator<AlarmValue> it = alarms.iterator();
        if (it.hasNext()) {
            av = it.next();
        } 
        logger.debug("Alarm fetch for alarm " + alarmId + ": " + (av == null ? "NULL" : "SUCCESS"));
        return av;
    }
    
}
