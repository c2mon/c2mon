package cern.c2mon.server.laser.publication;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterfaceFactory;
import cern.laser.source.alarmsysteminterface.FaultState;

import ch.cern.tim.server.alarm.AlarmPublisher;
import ch.cern.tim.shared.alarm.AlarmCacheObject;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Implementation of the AlarmPublisher interface.
 * @author J. Stowisek
 */
public class

LaserAlarmPublisherImpl extends AlarmPublisher {
  /** Reference to the LASER alarm system interface. */
  private AlarmSystemInterface asi = null;

  /**
   * Log4j Logger for this class.
   */
  private static final Logger LOG = Logger.getLogger(LaserAlarmPublisherImpl.class);

  /**
   * Log4j Logger for keeping track of all fault states sent to LASER.
   */
  private static final Logger FS_LOG = Logger.getLogger("FaultStateLogger");

  /**
   * Constructor.
   */
  public LaserAlarmPublisherImpl(final String pAlarmSourceName) {
    try {
      asi = AlarmSystemInterfaceFactory.createSource(pAlarmSourceName);
    } catch (ASIException e) {
      LOG.error("ejbCreate() : Cannot create LASER AlarmSystemInterface for source name " + pAlarmSourceName, e);
    }
  }

  /**
   *
   */
  public void close() {
    if (LOG.isDebugEnabled()) {
      LOG.debug("close() called.");
    }
    if (asi != null) {
      try {
        asi.close();
      } catch (Exception e) {
        LOG.error("Error closing LASER AlarmSystemInterface:", e);
      }
    }
  }

  /**
   * The finalizer ensures that the connection to the LASER AlarmSystemInterface
   * is closed before the object is removed from memory.
   */
  protected void finalize() {
    close();
  }

  /**
   * Send a list of active alarms ("active list") to the LASER system.
   * @param pActiveAlarms collection of ALL active alarms in the TIM system.
   */
  public final boolean sendActiveList(Collection pActiveAlarms) {
    boolean result = false;
    if (pActiveAlarms != null) {
      Properties prop = null;
      AlarmCacheObject alarm = null;

      // Create a list of LASER FaultState objects 
      Collection activeList = new ArrayList(pActiveAlarms.size());
      Iterator it = pActiveAlarms.iterator();
      while (it.hasNext()) {
        alarm = (AlarmCacheObject)it.next();
        FaultState fs = 
          AlarmSystemInterfaceFactory.createFaultState(alarm.getFaultFamily(), alarm.getFaultMember(), alarm.getFaultCode());
        fs.setDescriptor(alarm.getState());
        fs.setUserTimestamp(alarm.getTimestamp());
        if (alarm.getInfo() != null) {
          prop = fs.getUserProperties();
          prop.put(FaultState.ASI_PREFIX_PROPERTY, alarm.getInfo());
          fs.setUserProperties(prop);
        }
        activeList.add(fs);
      }
      // Push the list to LASER
      try {
        asi.pushActiveList(activeList);
        if (LOG.isDebugEnabled()) {
          LOG.debug("sendActiveList() : Active list pushed to LASER.");
        }
        result = true;
      } catch (ASIException asie) {
        LOG.error("sendActiveList() : LASER error sending active list.", asie);
      } catch (Exception e) {
        LOG.error("sendActiveList() : Unexpected exception sending active list.", e);
      }
    }
    return result;
  }

  /**
   * Send a TIM alarm to LASER.
   * @param pAlarm alarm to be sent to LASER
   */
  public final boolean sendFaultState(AlarmCacheObject pAlarm) {
    boolean result = true;
    if (pAlarm != null) {
      // Create the LASER FaultState object and fill it with the parameters from
      // the TIM alarm.
      FaultState fs = 
        AlarmSystemInterfaceFactory.createFaultState(pAlarm.getFaultFamily(), pAlarm.getFaultMember(), pAlarm.getFaultCode());

      if (pAlarm.getAlarmChange() == AlarmCacheObject.CHANGE_STATE) {
        fs.setDescriptor(pAlarm.getState());
      } else {
        fs.setDescriptor(FaultState.CHANGE);
      }
      fs.setUserTimestamp(pAlarm.getTimestamp());
      if (pAlarm.getInfo() != null) {
        Properties prop = fs.getUserProperties();
        prop.put(FaultState.ASI_PREFIX_PROPERTY, pAlarm.getInfo());
        fs.setUserProperties(prop);
      }

      // Send the FaultState object to LASER
      try {
        asi.push(fs);
      } catch (ASIException e) {
        // Ooops, didn't work. log the exception.
        result = false;
        StringBuffer str = 
          new StringBuffer("sendFaultState() : Alarm System Interface Exception. Unable to send FaultState ");
        str.append(pAlarm.getFaultFamily());
        str.append(':');
        str.append(pAlarm.getFaultMember());
        str.append(':');
        str.append(pAlarm.getFaultCode());
        str.append(" to LASER.");
        LOG.error(str, e);
      } catch (Exception e) {
        result = false;
        StringBuffer str = new StringBuffer("sendFaultState() : Unexpected Exception. Unable to send FaultState ");
        str.append(pAlarm.getFaultFamily());
        str.append(':');
        str.append(pAlarm.getFaultMember());
        str.append(':');
        str.append(pAlarm.getFaultCode());
        str.append(" to LASER.");
        LOG.error(str, e);
      }
      // Keep track of the sent alarm in the Alarm log
      StringBuffer str = new StringBuffer();
      str.append(pAlarm.getTimestamp());
      str.append("\t");
      str.append(pAlarm.getFaultFamily());
      str.append(':');
      str.append(pAlarm.getFaultMember());
      str.append(':');
      str.append(pAlarm.getFaultCode());
      str.append('\t');
      str.append(pAlarm.getState());
      if (pAlarm.getInfo() != null) {
        str.append('\t');
        str.append(pAlarm.getInfo());
      }
      if (result)
        FS_LOG.info(str);
      else
        FS_LOG.error(str);
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("sendFaultState() called with null parameter.");
      }
    }
    return result;
  }

  public boolean terminateFaultState(AlarmCacheObject pAlarm) {
    boolean result = true;
    if (pAlarm != null) {
      // Create the LASER FaultState object and fill it with the parameters from
      // the TIM alarm.
      FaultState fs = 
        AlarmSystemInterfaceFactory.createFaultState(pAlarm.getFaultFamily(), pAlarm.getFaultMember(), pAlarm.getFaultCode());

      fs.setDescriptor(FaultState.TERMINATE);
      fs.setUserTimestamp(new Timestamp(System.currentTimeMillis()));
      // Send the parameterised FaultState object to LASER
      try {
        asi.push(fs);
      } catch (ASIException e) {
        // Ooops, didn't work. log the exception.
        result = false;
        StringBuffer str = new StringBuffer();
        str.append("sendFaultState() : Unable to send faultstate ");
        str.append(pAlarm.getFaultFamily());
        str.append('_');
        str.append(pAlarm.getFaultMember());
        str.append('_');
        str.append(pAlarm.getFaultCode());
        str.append(" to LASER.");
        LOG.error(str, e);
      } catch (Exception e) {
        result = false;
        StringBuffer str = new StringBuffer();
        str.append("sendFaultState() : Unable to send faultstate ");
        str.append(pAlarm.getFaultFamily());
        str.append('_');
        str.append(pAlarm.getFaultMember());
        str.append('_');
        str.append(pAlarm.getFaultCode());
        str.append(" to LASER.");
        LOG.error(str, e);
      }
      // Keep track of the sent alarm in the Alarm log
      StringBuffer str = new StringBuffer();
      str.append(pAlarm.getTimestamp());
      str.append("\t");
      str.append(pAlarm.getFaultFamily());
      str.append(':');
      str.append(pAlarm.getFaultMember());
      str.append(':');
      str.append(pAlarm.getFaultCode());
      str.append('\t');
      str.append(pAlarm.getState());
      if (result)
        FS_LOG.info(str);
      else
        FS_LOG.error(str);
    } else {
      if (LOG.isDebugEnabled()) {
        LOG.debug("terminateFaultState() called with null parameter.");
      }
    }
    return result;
  }
}
