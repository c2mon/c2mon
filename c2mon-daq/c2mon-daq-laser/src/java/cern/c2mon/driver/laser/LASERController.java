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
package cern.c2mon.driver.laser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cern.c2mon.driver.common.EquipmentLogger;
import cern.c2mon.driver.common.IEquipmentMessageSender;
import cern.c2mon.driver.common.conf.equipment.IEquipmentConfiguration;
import cern.c2mon.driver.tools.equipmentexceptions.EqIOException;
import cern.laser.client.LaserConnectionException;
import cern.laser.client.LaserException;
import cern.laser.client.LaserTimeOutException;
import cern.laser.client.data.Alarm;
import cern.laser.client.data.Category;
import cern.laser.client.services.browsing.AlarmBrowsingHandler;
import cern.laser.client.services.browsing.CategoryBrowsingHandler;
import cern.laser.client.services.filtering.CategorySelection;
import cern.laser.client.services.filtering.Selection;
import cern.laser.client.services.selection.AlarmSelectionHandler;
import cern.tim.shared.common.datatag.address.LASERHardwareAddress;
import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.ChangeReport.CHANGE_STATE;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;

/**
 * LASER Controller to control tag and command configuration common and most used methods
 * 
 * @author vilches
 *
 */
public class LASERController {
  
  /**
   * Enum for describing connection to LASER
   */
  public static enum LASERConnection {CONNECTED, DISCONNECTED};
  
  /**
   * UNSUPPORTED_TAG_TYPE_INF
   */
  private static final String UNSUPPORTED_TAG_TYPE_INFO = 
      "The data type of this tag is not supported by the LASERMessageHandler";
  
  /**
   * The equipment logger of this class.
   */
  private EquipmentLogger equipmentLogger;

  /**
   * The equipment configuration of this handler.
   */
  private IEquipmentConfiguration equipmentConfiguration;

  /**
   * The equipment message sender to send to the server.
   */
  private IEquipmentMessageSender equipmentMessageSender;

  /**
   * Each Data Tag will have a unique key Family-Member-Code (COMPUTER-DCPSEA2-4006)
   */
  private HashMap<String, ISourceDataTag> tags4AlarmKey = new HashMap<String, ISourceDataTag>(); 

  /**
   * Map to store for each alarm category its bunch of Data Tags
   * 
   * ie: Category CERN.PS.CPS
   */
  private HashMap<String, List<ISourceDataTag>> tags4AlarmCategory = new HashMap<String, List<ISourceDataTag>>(); 
  
  /** 
   * Connection flag
   */
  private LASERConnection laserConnection = LASERConnection.DISCONNECTED;
  
  /**
   *  AlarmSelectionHandler
   */
  private AlarmSelectionHandler alarmSelectionHandler;
    
  /**
   * the alarm selection browser is used to check if the alarm exists in LASER
   */
  private AlarmBrowsingHandler alarmSelectionBrowser; 
  
  /**
   * Selection (Laser)
   */
  private Selection selection;
  
  /**
   * LASER AlarmSelectionListener
   */
  private LASERAlarmSelectionListener laserAlarmSelectionListener;
  
  /**
   * to handle categories
   */
  private CategoryBrowsingHandler categoryHandler;
  
  /**
   * to handle category selections
   */
  private CategorySelection category_selection;
  
  /**
   * Boolean to check New categories
   */
  private boolean isNewcategory;
  

  /**
   * Constructor
   * 
   * @param equipmentLogger 
   * @param equipmentConfiguration 
   * @param equipmentMessageSender
   * 
   */
  public LASERController(final EquipmentLogger equipmentLogger, final IEquipmentConfiguration equipmentConfiguration, 
      final IEquipmentMessageSender equipmentMessageSender) {
    this.equipmentLogger = equipmentLogger;
    this.equipmentConfiguration = equipmentConfiguration;
    this.equipmentMessageSender = equipmentMessageSender;
    this.laserAlarmSelectionListener = new LASERAlarmSelectionListener(this);
    this.isNewcategory = false;
  }
  
  /**
   * Initialize LASER variables
   * 
   * @throws EqIOException 
   */
  public final void initialization() throws EqIOException {
    try {
      //create the alarm selection browser 
      this.alarmSelectionBrowser = AlarmBrowsingHandler.get();
      
      //create the alarm selection handler
      this.alarmSelectionHandler = AlarmSelectionHandler.get();
      
      // create a selection to give to the select method
      this.selection = this.alarmSelectionHandler.createSelection();
      
      // find the categories to subscribe to
      this.categoryHandler = CategoryBrowsingHandler.get();
    }
    catch (LaserConnectionException lce) {
      getEquipmentLogger().error("connection - could not initialise connection with LASER.", lce);
      throw new EqIOException("could not initialise connection with LASER: " + lce.getMessage());
    }
    catch (LaserException le) {
      getEquipmentLogger().error("connection - could not get Alarm Selection Handler from LASER", le);
      throw new EqIOException("could not get Alarm Selection Handler from LASER: " + le.getMessage());
    }    
  }
  
  /**
   * Connection 
   * 
   * General connection for all data tags
   * 
   * @throws EqIOException 
   */
  public final void connection() throws EqIOException {
    // Initialize and connect LASER variables
    initialization();
                       
    // Clean maps to start from scratch
    this.tags4AlarmCategory.clear();
    this.tags4AlarmKey.clear();
    
    // Start the connection procedure
    for (ISourceDataTag sdt : this.equipmentConfiguration.getSourceDataTags().values()) {
      connection(sdt, null);
    }
    
    // Subscribe to the alarms
    categorySubscription();
    
    // Reset the number of incorrect Alarm counter
    int numberOfIncorrectAlarms = 0;
    for (ISourceDataTag sdt : this.equipmentConfiguration.getSourceDataTags().values()) {
      // get tag's hw. address
      LASERHardwareAddress hwadr = (LASERHardwareAddress) sdt.getHardwareAddress();
      
      // check if alarm is defined in LASER
      if (!isDefinedInLaser(sdt)) {  
         numberOfIncorrectAlarms++;
      
         getEquipmentLogger().warn("connection - [ ! ] the alarm defined by tripplet [" + hwadr.getFaultFamily() 
               + ":" + hwadr.getFaultMember() + ":" + hwadr.getFalutCode()
               + "] was not found in LASER. Invalidating corresponding tag: " + sdt.getId());
         // if not - invalidate the tag
         this.equipmentMessageSender.sendInvalidTag(sdt, SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
              "The alarm [" + hwadr.getFaultFamily() + ":" + hwadr.getFaultMember() + ":" + hwadr.getFalutCode()
              + "] does not exist in LASER");                           
      }   
    }

  
    // check if at least one alarm could be found on LASER side
    if (numberOfIncorrectAlarms < this.equipmentConfiguration.getSourceDataTags().size()) {
      //set the connection-state flag
      this.laserConnection = LASERConnection.CONNECTED;
      // confirm that the equipment's fine
      getEquipmentLogger().debug("connection - calling confirmEquipmentStateOK() to confirm the correct connection status");      
      this.equipmentMessageSender.confirmEquipmentStateOK();         
    }
    else {
      getEquipmentLogger().error("connection - could not initialise connection with LASER. " 
                              + "None of the alarms from the DAQ's list was found in LASER. It looks like a problem on LASER db side");
      throw new EqIOException("could not initialise connection with LASER. " 
                              + "Problem description: None of the alarms from the DAQ's list was found in LASER. "
                              + "It looks like a problem on LASER db side");    
    }    
  }

  /**
   * Connection
   * 
   * Particular connection to each data tag
   * 
   * @param sourceDataTag 
   * @param changeReport 
   * 
   * @return CHANGE_STATE (FAIL or SUCCESS)
   */
  public final CHANGE_STATE connection(final ISourceDataTag sourceDataTag, final ChangeReport changeReport) {
    // get tag's hw. address
    LASERHardwareAddress hwadr = (LASERHardwareAddress) sourceDataTag.getHardwareAddress();

    // if the tag is not boolean, invalidate it
    if (!isValidDataType(sourceDataTag)) {
      this.equipmentLogger.error("connection - numeric tag (id: " + sourceDataTag.getId() 
          + ") was found in the configuration. Numeric tags are not supported. invalidating.");
      this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.UNSUPPORTED_TYPE ,
            LASERController.UNSUPPORTED_TAG_TYPE_INFO); 

      if (changeReport != null) {
        changeReport.appendError("connection - numeric tag (id: " + sourceDataTag.getId() 
            + ") was found in the configuration. Numeric tags are not supported. invalidating.");
      }
      // Fail
      return CHANGE_STATE.FAIL;
    }
    else {
      if (this.equipmentLogger.isDebugEnabled()) {
        this.equipmentLogger.debug("connection - sending initial tag (id: " + sourceDataTag.getId() + ") state (false) to C2MON..");
      }                
    }

    // Create the key for the alarm=>tag lookup table
    String key = getDataTagLookupKey(hwadr.getFaultFamily(), hwadr.getFaultMember(), hwadr.getFalutCode());

    // if this key has not yet been registered, do it ! 
    if (!this.tags4AlarmKey.containsKey(key)) {
      this.tags4AlarmKey.put(key, sourceDataTag);

      try {
        Category category = this.categoryHandler.getCategoryByPath(hwadr.getAlarmCategory());

        // if the alarm's category is not yet in the list we add it
        if (!this.selection.getCategorySelection().contains(category)) {
          this.selection.getCategorySelection().add(category);
          getEquipmentLogger().debug("connection - category: " + hwadr.getAlarmCategory() + " has been added to the Selection");
          
          if (changeReport != null) {
            this.isNewcategory = true;
          }
        }
      }
      catch (LaserException ex) {
        getEquipmentLogger().error("connection - could not lookup for alarm category: " + hwadr.getAlarmCategory(), ex);
        getEquipmentLogger().info("connection - invalidating all tags belonging to category: " + hwadr.getAlarmCategory());
        this.invalidateAllTags4Category(hwadr.getAlarmCategory());
      }

      // We create the alarm Category if it does not exist. Each one can have one or more ISourceDataTags
      if (!this.tags4AlarmCategory.containsKey(hwadr.getAlarmCategory())) {
        this.tags4AlarmCategory.put(hwadr.getAlarmCategory(), new ArrayList<ISourceDataTag>());
      }     
      // We add then the ISourceDataTag in a proper category
      this.tags4AlarmCategory.get(hwadr.getAlarmCategory()).add(sourceDataTag);
    }
    // otherwise, invalidate the tag, as it means someone's defined 2 addresses with the same triplet:
    // [fault-family:fault-member:fault-state] what is prohibited
    else {
      this.equipmentLogger.error("connection - the hardware address of tag: #" + sourceDataTag.getId() + " is incorrect."
          + "The hardware address " + hwadr.getFaultFamily() + ":" + hwadr.getFaultMember() + ":" + hwadr.getFalutCode()
          + " is already defined for another tag #" + this.tags4AlarmKey.get(key).getId());  

      this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.INCORRECT_NATIVE_ADDRESS, 
          "disconnection - The hardware address " + hwadr.getFaultFamily() + ":" + hwadr.getFaultMember() + ":" + hwadr.getFalutCode()
          + " is already defined for tag #" + this.tags4AlarmKey.get(key).getId());    
      
      if (changeReport != null) {
        changeReport.appendError("connection - the hardware address of tag: #" + sourceDataTag.getId() + " is incorrect."
            + "The hardware address " + hwadr.getFaultFamily() + ":" + hwadr.getFaultMember() + ":" + hwadr.getFalutCode()
            + " is already defined for another tag #" + this.tags4AlarmKey.get(key).getId());  
      }
      
      // Fail
      return CHANGE_STATE.FAIL;
    }
    
    // We subscribe if the function is call from reconfiguration and we have a new category in our selection
    if ((changeReport != null) && (this.isNewcategory)) {
      // Reset new category
      this.isNewcategory = false;
      
      try {
        this.alarmSelectionHandler.resetSelection();
      }
      catch (LaserTimeOutException lte) {
        getEquipmentLogger().error("connection - LASER time out exception while reseting currently active alarms", lte);
        changeReport.appendError("connection - LASER time out exception while reseting currently active alarms");
        
        // Fail
        return CHANGE_STATE.FAIL;
      }
      catch (LaserException ex) {
        getEquipmentLogger().error("connection - could not initialise connection with LASER.", ex);
        changeReport.appendError("connection - could not initialise connection with LASER.");
        
        // Fail
        return CHANGE_STATE.FAIL;
      }
      
      try {
        categorySubscription();
      }
      catch (EqIOException e) {
        getEquipmentLogger().error("connection - could not subscribe categories", e);
        changeReport.appendError("connection - could not subscribe categories");
        
        // Fail
        return CHANGE_STATE.FAIL;
      }
    }
    
    return CHANGE_STATE.SUCCESS;
  }
  
  /**
   * The select function will go through all the categories and will do the subscription to all 
   * the alarms so we can receive them from the DAQ
   * 
   * @throws EqIOException 
   */
  private void categorySubscription() throws EqIOException {
    try {
      getEquipmentLogger().debug("subscription - selecting alarms...");
      
      Map result = this.alarmSelectionHandler.select(this.selection, this.laserAlarmSelectionListener);
    
      getEquipmentLogger().debug("selected " + result.size() + " alarms.");
      for (Iterator iter = result.values().iterator(); iter.hasNext();) {
        Alarm element = (Alarm) iter.next();
        getEquipmentLogger().debug("init alarm returned :\n" + element.getAlarmId());
     
        // Check if alarm is active, if so - send it to TIM
        if (element.getStatus().isActive()) {
          this.laserAlarmSelectionListener.onAlarm(element);
        }         
      }      
    } 
    catch (LaserTimeOutException lte) {
      getEquipmentLogger().error("subscription - LASER time out exception while selecting currently active alarms", lte);
      throw new EqIOException("LASER time out exception while selecting currently active alarms: " + lte.getMessage());
    }
    catch (LaserException ex) {
      getEquipmentLogger().error("subscription - could not initialise connection with LASER.", ex);
      throw new EqIOException("could not initialise connection with LASER: " + ex.getMessage());
    }
  }
  
  /**
   * Disconnection 
   * 
   * General disconnection for all data tags
   * 
   * @throws EqIOException 
   */
  public final void disconnection() throws EqIOException {
    try {
      this.alarmSelectionHandler.close();
    }
    catch (LaserException ex) {
      getEquipmentLogger().error("disconnection - error while trying to close alarm selection handler", ex);
      throw new EqIOException("error while trying to close alarm selection handler: " + ex.getMessage());
    }
  }


  /**
   * The LASER tag needs to be of type boolean. 
   * 
   * @param sourceDataTag the source Data Tag
   * @return true if it is valid or false if it is not
   */ 
  private boolean isValidDataType(final ISourceDataTag sourceDataTag) {
    // if the tag is not boolean, invalidate it
    if (sourceDataTag.getDataTypeNumeric() != TagDataType.TYPE_BOOLEAN) {
      return false;
    } 

    return true;
  }
  
  /**
   * This method generates a key for alarm=>tags lookup table
   * 
   * @param faultCode 
   * @param faultMember 
   * @param faultFamily 
   * @return key used for lookups
   */
  public final String getDataTagLookupKey(final String faultFamily, final String faultMember, final int faultCode) {
    StringBuffer strBuff = new StringBuffer("<fault-family>");
    strBuff.append(faultFamily).append("</fault-family><fault-member>").
            append(faultMember).append("</fault-member><fault-code>").
            append(faultCode).append("</fault-code>");
            
    return strBuff.toString();
  }
  
  /**
   * This method invalidates all tags belonging to the specified alarm category
   * @param pCategoryName - the category name
   */
  private void invalidateAllTags4Category(final String pCategoryName) {
     if (this.equipmentLogger.isDebugEnabled()) {
       this.equipmentLogger.debug("entering invalidateAllTags4Category()..");
     }
     
     // Invalidate tags
     for (ISourceDataTag sourceDataTag :  this.tags4AlarmCategory.get(pCategoryName)) {
       this.equipmentMessageSender.sendInvalidTag(sourceDataTag, SourceDataQuality.DATA_UNAVAILABLE,                              
                           "The DAQ encountered problem looking up for alarm category: " + pCategoryName);
     }
     
     if (this.equipmentLogger.isDebugEnabled()) {
       this.equipmentLogger.debug("leaving invalidateAllTags4Category()");
     }          
  }
  
  /**
   * This method checks if the given tag is defined as alarm in LASER. Tag's hardware address must contain a proper 
   * tripplet [FAULT_FAMILY:FAULT_MEMBER:FAULT_CODE]. 
   * 
   * @param sdt 
   * @return true if it is define in LASER or false if not
   */
  private boolean isDefinedInLaser(final ISourceDataTag sdt) {
    if (this.equipmentLogger.isDebugEnabled()) {
      getEquipmentLogger().debug("isDefinedInLaser - entering isValidLaserTag()..");
    }
    
    boolean result = true;

    LASERHardwareAddress adr = (LASERHardwareAddress) sdt.getHardwareAddress();
     
    // check if the alarm exists in LASER
    if (getEquipmentLogger().isDebugEnabled()) {
      this.equipmentLogger.debug("isDefinedInLaser - checking if alarm defined by tripplet ["
            + adr.getFaultFamily() + ":" + adr.getFaultMember() + ":" + adr.getFalutCode() + "] exists in LASER..");   
    }
    
    Alarm alarm = null;
        
    try {
      alarm = alarmSelectionBrowser.getAlarmByTriplet(adr.getFaultFamily(), adr.getFaultMember(), new Integer(adr.getFalutCode()));        
    }
    catch (LaserException ex) {
      this.equipmentLogger.error("isDefinedInLaser - LASER exception caught, while trying to check if alarm defined by tripplet exists in LASER", ex);
      alarm = null;
    }
                                                                   
    if (alarm == null)  {
       result = false;
    }
      
    if (this.equipmentLogger.isDebugEnabled()) {   
      this.equipmentLogger.debug("isDefinedInLaser - leaving isValidLaserTag() with result value: " + result);
    }
      
    return result;
  }

  /**
   * @return the equipmentMessageSender
   */
  public final IEquipmentMessageSender getEquipmentMessageSender() {
    return this.equipmentMessageSender;
  }

  /**
   * @return the equipmentLogger
   */
  public final EquipmentLogger getEquipmentLogger() {
    return this.equipmentLogger;
  }

  /**
   * @return the equipmentConfiguration
   */
  public final IEquipmentConfiguration getEquipmentConfiguration() {
    return this.equipmentConfiguration;
  }
  
  /**
   * 
   * @return tags4AlarmKey
   */
  public final HashMap<String, ISourceDataTag> getTags4AlarmKey() {
    return this.tags4AlarmKey;
  } 
  
  /**
   * 
   * @return laserConnection
   */
  public final LASERConnection getLaserConnection() {
    return this.laserConnection;
  }
  
  /**
   * 
   * @param laserConnection
   */
  public final void setLaserConnection(final LASERConnection laserConnection) {
    this.laserConnection = laserConnection;
  }

}
