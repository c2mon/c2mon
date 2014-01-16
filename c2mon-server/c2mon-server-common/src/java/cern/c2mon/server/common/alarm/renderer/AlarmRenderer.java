package cern.c2mon.server.common.alarm.renderer;

import org.apache.log4j.or.ObjectRenderer;

import cern.c2mon.server.common.alarm.Alarm;

/**
 * Log4j ObjectRenderer for the Alarm class. The purpose of an object render is to log objects of a certain class in a
 * uniform way.
 */
public class AlarmRenderer implements ObjectRenderer {

    /**
     * Default constructor.
     */
    public AlarmRenderer() {/* Nothing to do */
    }

    /**
     * Implementation of the ObjectRenderer interface
     * 
     * @param o the DataTagCacheObject to be rendered
     * @return a string representation of the DataTagCacheObject, null if the object passed as a parameter is null.
     */
    @Override
    public String doRender(Object o) {
        if (o != null) {
            if (o instanceof Alarm) {
                Alarm alarm = (Alarm) o;

                StringBuffer str = new StringBuffer();

                str.append(alarm.getId());
                str.append('\t');
                str.append(alarm.getTagId());
                str.append('\t');
                str.append(alarm.getTimestamp());
                str.append('\t');
                str.append(alarm.getFaultFamily());
                str.append('\t');
                str.append(alarm.getFaultMember());
                str.append('\t');
                str.append(alarm.getFaultCode());
                str.append('\t');
                str.append(alarm.getState());
                if (alarm.getInfo() != null) {
                  str.append('\t');
                  str.append(alarm.getInfo());
                }

                return str.toString();
            } else {
                // if someone passed an object other than Alarm
                return o.toString();
            }
        } else {
            // if somebody decided to pass a null parameter
            return null;
        }
    }
}
