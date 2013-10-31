/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2013 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.laser;

import cern.c2mon.daq.laser.LASERController.LASERConnection;
import cern.c2mon.daq.tools.equipmentexceptions.EqIOException;
import cern.laser.client.data.Alarm;
import cern.laser.client.services.selection.AlarmSelectionListener;
import cern.laser.client.services.selection.LaserHeartbeatException;
import cern.laser.client.services.selection.LaserSelectionException;

/**
 * This class implements the asynchronous alarm selection listener interface
 * 
 * @author vilches
 *
 */
public class LASERAlarmSelectionListener implements AlarmSelectionListener {
  
  /**
   * Info message for LASER connection lost
   */
  private static final String CONNECTION_TO_LASER_LOST_INFO =
      "The connection to LASER was lost";
      
  /**
   * Info message for LASER heartbeat lost
   */
  private static final String LASER_HEARTBIT_LOST_INFO = 
      "LASER heartbeat was lost";
  
  /**
   * Waiting time for thread sleeping time
   */
  private static final int SLEEP_TIME = 15000;

  /**
   * LASER controller
   */
  private LASERController laserController;
  
  /**
   * Constructor
   * 
   * @param laserController
   *
   */
  public LASERAlarmSelectionListener(final LASERController laserController) {
   this.laserController = laserController;
  }

  @Override
  public final void onAlarm(final Alarm alarm) {
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug("onAlarm - entering ...");
    }
    
    this.laserController.getEquipmentLogger().debug("onAlarm - received alarm alarm_id: " + 
            alarm.getAlarmId() + ", alarm_status: " + alarm.getStatus().toString());
    
    String key  = this.laserController.getDataTagLookupKey(alarm.getFaultFamily(),
            alarm.getFaultMember(), alarm.getFaultCode().intValue());
    if (!this.laserController.getTags4AlarmKey().containsKey(key)) {
      this.laserController.getEquipmentLogger().warn("onAlarm - this alarm is unknown to the DAQ, skipping it"); 
    }
    else { 
      this.laserController.getEquipmentMessageSender().sendTagFiltered(this.laserController.getTags4AlarmKey().get(key), 
                             new Boolean(alarm.getStatus().isActive()),
                             alarm.getStatus().getSourceTimestamp().getTime());          
    }
    
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug("onAlarm - leaving");
    }    
    
  }

  @Override
  public final void onException(final LaserSelectionException e) {
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug("onException - entering ...");
    }
    
    if (e.getCode().equals(LaserHeartbeatException.HEARTBEAT_RECONNECTED) 
        || e.getCode().equals(LaserSelectionException.CONNECTION_REESTABILISHED)) {      
      this.laserController.getEquipmentLogger().info("onException - LASER heartbeat reconnected, connection reopened");
      
      // try to reinitialize connection: re-subscribe all alarms  
      this.laserController.getEquipmentLogger().debug("onException - try to reinitialise connection: resubscribe all alarms");
      while (this.laserController.getLaserConnection() != LASERConnection.CONNECTED) {   
        // Disconnect from data source  
        try {
          this.laserController.getEquipmentLogger().debug("onException - calling disconnection() in order to clean up resources");
          this.laserController.disconnection();         
        }
        catch (EqIOException eqIoEx) {
          this.laserController.getEquipmentLogger().error("onException - error trying to call disconnection()", eqIoEx);              
        }

        // Connect to data source again
        try {          
          this.laserController.getEquipmentLogger().debug("onException - calling connection() to reopen the connection to LASER and resubscribe the alarms");
          this.laserController.connection();    
        }
        catch (EqIOException eqIoEx) {
          this.laserController.getEquipmentLogger().error("onException - error trying to call connection()", eqIoEx);              
        }
        
        if (this.laserController.getLaserConnection() == LASERConnection.DISCONNECTED) {
          try {
            this.laserController.getEquipmentLogger().debug("onException - the handler will try to reconnect in 5 sec");
            Thread.sleep(SLEEP_TIME);
          }
          catch (InterruptedException intEx) {}
        }
      }
    }
    
    
    if (e.getCode().equals(LaserHeartbeatException.HEARTBEAT_LOST)) {           
      this.laserController.getEquipmentLogger().error("onException - LASER heartbeat lost", e);
      this.laserController.getEquipmentLogger().debug("onException - sending CommfaultTag..");
      this.laserController.getEquipmentMessageSender().confirmEquipmentStateIncorrect(LASERAlarmSelectionListener.LASER_HEARTBIT_LOST_INFO);   
      this.laserController.setLaserConnection(LASERConnection.DISCONNECTED);
    } 
    
    if (e.getCode().equals(LaserSelectionException.CONNECTION_DROPPED)) {
      this.laserController.getEquipmentLogger().error("onException - the connection to LASER lost", e);   
      this.laserController.getEquipmentLogger().debug("onException - sending CommfaultTag ...");    
      this.laserController.getEquipmentMessageSender().confirmEquipmentStateIncorrect(LASERAlarmSelectionListener.CONNECTION_TO_LASER_LOST_INFO);  
      this.laserController.setLaserConnection(LASERConnection.DISCONNECTED);
    }
        
    if (this.laserController.getEquipmentLogger().isDebugEnabled()) {
      this.laserController.getEquipmentLogger().debug("onException - leaving");
    }    
  }

 

}
