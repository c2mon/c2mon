/******************************************************************************
 * This file is part of the CERN Control and Monitoring (C2MON) platform.
 * 
 * See http://cern.ch/c2mon
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
 * Author: C2MON team, c2mon-support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common.impl;

import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cern.c2mon.daq.common.IDynamicTimeDeadbandFilterer;
import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.ICoreDataTagChanger;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.EquipmentSenderHelper;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagConstants;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * EquipmentMessageSender to control all filtering and sending.
 * 
 * @author vilches
 */
public class EquipmentMessageSender implements ICoreDataTagChanger, IEquipmentMessageSender, IDynamicTimeDeadbandFilterer {
    
	/**
     * EquipmentLoggerFactory of this class.
     */
    private EquipmentLoggerFactory equipmentLoggerFactory;
    
    /**
     * The logger for this class.
     */
    private EquipmentLogger equipmentLogger;
    
    /**
     * The filter message sender. All tags a filter rule matched are added to this.
     */
    private IFilterMessageSender filterMessageSender;

    /**
     * The process message sender takes the messages actually send to the server.
     */
    private IProcessMessageSender processMessageSender;

    /**
     * The dynamic time band filter activator activates time deadband filtering based on tag occurrence. This one is for
     * medium priorities.
     */
    private IDynamicTimeDeadbandFilterActivator medDynamicTimeDeadbandFilterActivator;

    /**
     * The dynamic time band filter activator activates time deadband filtering based on tag occurrence. This one is for
     * low priorities.
     */
    private IDynamicTimeDeadbandFilterActivator lowDynamicTimeDeadbandFilterActivator;

    /**
     * The equipment configuration of this sender.
     */
    private EquipmentConfiguration equipmentConfiguration;
    
    /**
     * Valid Sender helper class
     */
    private EquipmentSenderValid equipmentSenderValid;
    
    /**
     * Invalid Sender helper class
     */
    private EquipmentSenderInvalid equipmentSenderInvalid;
    
    /**
     * The equipment sender helper with many common and useful methods shared by sending classes
     */
    private EquipmentSenderHelper equipmentSenderHelper = new EquipmentSenderHelper();
    
    /**
     * The Equipment Alive sender helper class
     */
    private EquipmentAliveSender equipmentAliveSender;
    
    /**
     * Time deadband helper class
     */
    private EquipmentTimeDeadband equipmentTimeDeadband;

    /**
     * Creates a new EquipmentMessageSender.
     * 
     * @param filterMessageSender The filter message sender to send filtered tag values.
     * @param processMessageSender The process message sender to send tags to the server.
     * @param medDynamicTimeDeadbandFilterActivator The dynamic time deadband activator for medium priorities.
     * @param lowDynamicTimeDeadbandFilterActivator The dynamic time deadband activator for low priorities. checks
     *            around the data tag.
     */
    @Autowired
    public EquipmentMessageSender(
            final IFilterMessageSender filterMessageSender,
            final IProcessMessageSender processMessageSender,
            @Qualifier("medDynamicTimeDeadbandFilterActivator") final IDynamicTimeDeadbandFilterActivator medDynamicTimeDeadbandFilterActivator,
            @Qualifier("lowDynamicTimeDeadbandFilterActivator") final IDynamicTimeDeadbandFilterActivator lowDynamicTimeDeadbandFilterActivator) {
        super();
        this.filterMessageSender = filterMessageSender;
        this.processMessageSender = processMessageSender;
        this.medDynamicTimeDeadbandFilterActivator = medDynamicTimeDeadbandFilterActivator;
        this.lowDynamicTimeDeadbandFilterActivator = lowDynamicTimeDeadbandFilterActivator;
    }
    
    /**
     * Init
     * 
     * @param equipmentConfiguration
     * @param equipmentLoggerFactory
     */
    public void init(final EquipmentConfiguration equipmentConfiguration, final EquipmentLoggerFactory equipmentLoggerFactory) {
    	// Configuration 
    	setEquipmentConfiguration(equipmentConfiguration);
    	// Logger
    	setEquipmentLoggerFactory(equipmentLoggerFactory);

    	// Time Deadband
        this.equipmentTimeDeadband = new EquipmentTimeDeadband(this, equipmentLoggerFactory);
    	// Valid Sender
    	this.equipmentSenderValid = new EquipmentSenderValid(this.filterMessageSender, this.processMessageSender, this,
    			equipmentLoggerFactory);
    	// Invalid Sender
    	this.equipmentSenderInvalid = new EquipmentSenderInvalid(this.filterMessageSender, this.processMessageSender, this,
    			equipmentLoggerFactory);
    	 
    	// Inits
    	this.equipmentTimeDeadband.init(equipmentSenderValid);
    	this.equipmentSenderValid.init(equipmentSenderInvalid, equipmentTimeDeadband);
    	this.equipmentSenderInvalid.init(equipmentTimeDeadband);
    	
    	// Alive Sender
    	this.equipmentAliveSender = new EquipmentAliveSender(this.processMessageSender, this.equipmentConfiguration.getAliveTagId(), 
    	        equipmentLoggerFactory);
    	this.equipmentAliveSender.init(this.equipmentConfiguration.getAliveTagInterval(), this.equipmentConfiguration.getName());
    }

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     */
    @Override
    public void sendSupervisionAlive() {
        sendSupervisionAlive(System.currentTimeMillis());
    }

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     * 
     * @param milisecTimestamp the timestamp (in milliseconds)
     */
    @Override
    public void sendSupervisionAlive(final long milisecTimestamp) {
        Long supAliveTagId = Long.valueOf(this.equipmentConfiguration.getAliveTagId());
        SourceDataTag supAliveTag = getTag(supAliveTagId);
        
        this.equipmentAliveSender.sendEquipmentAlive(supAliveTag, milisecTimestamp);
    }

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @return True if the tag has been send successfully to the server.
     */
    @Override
    public boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp) {
        return sendTagFiltered(currentTag, tagValue, milisecTimestamp, null);
    }

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param tagValue The tag value to send.
     * @param milisecTimestamp The timestamp of the tag.
     * @param pValueDescr A description belonging to the value.
     * @return True if the tag has been send successfully to the server.
     */
    @Override
    public boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            String pValueDescr) {
        return sendTagFiltered(currentTag, tagValue, milisecTimestamp, pValueDescr, false);
    }

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @param pValueDescr A description belonging to the value.
     * @return True if the tag has been send successfully to the server.
     */
    @Override
    public boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
    		String pValueDescr, boolean sentByValueCheckMonitor) {

    	this.equipmentLogger.trace("sendTagFiltered - entering sendTagFiltered()");

    	boolean successfulSent = false;
    	long tagID = currentTag.getId();
    	SourceDataTag tag = getTag(tagID);

    	// If we received an update of equipment alive tag, we send immediately a message to the server
    	if (this.equipmentConfiguration.getAliveTagId() == tagID) {
    		successfulSent = this.equipmentAliveSender.sendEquipmentAlive(tag, milisecTimestamp);
    	} else {
    		successfulSent = this.equipmentSenderValid.sendTagFiltered(tag, tagValue, milisecTimestamp, pValueDescr,
    				sentByValueCheckMonitor);		
    	}

    	this.equipmentLogger.trace("sendTagFiltered - leaving sendTagFiltered()");
    	
    	return successfulSent;
    }
    
    /**
     * This method sends an invalid SourceDataTagValue to the server. Source and DAQ timestamps are set to the current
     * DAQ system time.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param pDescription the quality description (optional)
     */
    @Override
    public void sendInvalidTag(final ISourceDataTag sourceDataTag, final short pQualityCode, final String pDescription) {
    	sendInvalidTag(sourceDataTag, pQualityCode, pDescription, null);
    }

    /**
     * This method sends an invalid SourceDataTagValue to the server, without changing its origin value.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param qualityDescription the quality description (optional)
     * @param pTimestamp time when the SourceDataTag's value has become invalid; if null the source timestamp and DAQ
     *            timestamp will be set to the current DAQ system time
     */
    @Override
    public void sendInvalidTag(final ISourceDataTag sourceDataTag, final short qualityCode, final String qualityDescription, 
        final Timestamp pTimestamp) {
    	
    	// Get the source data quality from the quality code     
    	SourceDataQuality newSDQuality = this.equipmentSenderHelper.createTagQualityObject(qualityCode, qualityDescription);         

    	// The sendInvalidTag function with the value argument will take are of it     
    	if (sourceDataTag.getCurrentValue() != null) {       
    		sendInvalidTag(sourceDataTag, sourceDataTag.getCurrentValue().getValue(), 
    				sourceDataTag.getCurrentValue().getValueDescription(), newSDQuality, pTimestamp);     
    	} else {       
    		sendInvalidTag(sourceDataTag, null, "", newSDQuality, pTimestamp);     
    	}
    }

    /**
     * This method sends both an invalid and updated SourceDataTagValue to the server.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param newValue The new update value that we want set to the tag 
     * @param newTagValueDesc The new value description
     * @param newSDQuality the new SourceDataTag see {@link SourceDataQuality}
     * @param pTimestamp time when the SourceDataTag's value has become invalid; if null the source timestamp and DAQ
     *            timestamp will be set to the current DAQ system time
     */
    protected void sendInvalidTag(final ISourceDataTag sourceDataTag,
                               final Object newValue,
                               final String newTagValueDesc,
                               final SourceDataQuality newSDQuality,
                               final Timestamp pTimestamp) {
      this.equipmentLogger.debug("sendInvalidTag - entering sendInvalidTag() for tag #" + sourceDataTag.getId());

      long tagID = sourceDataTag.getId();
      SourceDataTag tag = getTag(tagID);
  
      if (newSDQuality == null || newSDQuality.isValid()) {
    	  // this means we have a valid quality code 0 (OK)
    	  this.equipmentLogger.warn("sendInvalidTag - method called with 0(OK) quality code for tag " + sourceDataTag.getId()
    			  + ". This should normally not happen! Redirecting call to sendTagFiltered() method.");
    	  this.equipmentSenderValid.sendTagFiltered(tag, newValue, pTimestamp.getTime(), newTagValueDesc);
      }
      
      this.equipmentSenderInvalid.sendInvalidTag(tag, newValue, newTagValueDesc, newSDQuality, pTimestamp);
      
      this.equipmentLogger.debug("sendInvalidTag - leaving sendInvalidTag()");
    }

    /**
     * Depending on the tag priority it will be recorded for dynamic time deadband filtering.
     * 
     * @param tag The tag to be recorded.
     */
    @Override
    public void recordTag(final SourceDataTag tag) {
        DataTagAddress address = tag.getAddress();
        if (!address.isStaticTimedeadband() && this.equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
            switch (address.getPriority()) {
            case DataTagConstants.PRIORITY_LOW:
                this.lowDynamicTimeDeadbandFilterActivator.newTagValueSent(tag.getId());
                break;
            case DataTagConstants.PRIORITY_MEDIUM:
                this.medDynamicTimeDeadbandFilterActivator.newTagValueSent(tag.getId());
                break;
            default:
                // other priorities are ignored
                break;
            }
        }
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     */
    @Override
    public final void confirmEquipmentStateIncorrect() {
        confirmEquipmentStateIncorrect(null);
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     * 
     * @param pDescription additional description
     */
    @Override
    public final void confirmEquipmentStateIncorrect(final String pDescription) {
        sendCommfaultTag(this.equipmentConfiguration.getCommFaultTagId(), this.equipmentConfiguration.getCommFaultTagValue(),
                pDescription);
        Enumeration<Long> enume = this.equipmentConfiguration.getSubEqCommFaultValues().keys();
        // Send the commFaultTag for the equipment's subequipments too
        if (enume != null) {
            while (enume.hasMoreElements()) {
                Long commFaultId = enume.nextElement();
                sendCommfaultTag(commFaultId, this.equipmentConfiguration.getSubEqCommFaultValues().get(commFaultId),
                        pDescription);
            }
        }
    }
    
    /**
     * Sends the CommfaultTag message.
     * 
     * @param tagID The CommfaultTag id.
     * @param value The CommFaultTag value to send.
     * @param description The description of the CommfaultTag
     */
    private void sendCommfaultTag(final long tagID, final Boolean value, final String description) {
        if (this.equipmentLogger.isDebugEnabled()) {
            this.equipmentLogger.debug("sendCommfaultTag - entering sendCommfaultTag()..");
            this.equipmentLogger.debug("\tCommFaultTag: #" + tagID);
        }
        if (description == null) {
            this.processMessageSender.sendCommfaultTag(tagID, value);
        } else {
        	this.processMessageSender.sendCommfaultTag(tagID, value, description);
        }
        this.equipmentLogger.debug("sendCommfaultTag - leaving sendCommfaultTag()");
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     */
    @Override
    public final void confirmEquipmentStateOK() {
        confirmEquipmentStateOK(null);
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     * 
     * @param pDescription additional description
     */
    @Override
    public final void confirmEquipmentStateOK(final String pDescription) {
        sendCommfaultTag(this.equipmentConfiguration.getCommFaultTagId(), !this.equipmentConfiguration.getCommFaultTagValue(),
                pDescription);
        Enumeration<Long> enume = this.equipmentConfiguration.getSubEqCommFaultValues().keys();
        // Send the commFaultTag for the equipment's subequipments too
        if (enume != null) {
            while (enume.hasMoreElements()) {
                Long commFaultId = enume.nextElement();
                sendCommfaultTag(commFaultId, !(this.equipmentConfiguration.getSubEqCommFaultValues().get(commFaultId)),
                        pDescription);
            }
        }
    }

    /**
     * Sets the equipment configuration
     * 
     * @param equipmentConfiguration The equipment configuration.
     */
    private void setEquipmentConfiguration(final EquipmentConfiguration equipmentConfiguration) {
        this.equipmentConfiguration = equipmentConfiguration;
        Map<Long, SourceDataTag> sourceDataTags = equipmentConfiguration.getDataTags();
        this.medDynamicTimeDeadbandFilterActivator.clearDataTags();
        this.lowDynamicTimeDeadbandFilterActivator.clearDataTags();
        for (Entry<Long, SourceDataTag> entry : sourceDataTags.entrySet()) {
            DataTagAddress address = entry.getValue().getAddress();
            if (!address.isStaticTimedeadband() && equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
                switch (address.getPriority()) {
                case DataTagConstants.PRIORITY_LOW:
                    this.lowDynamicTimeDeadbandFilterActivator.addDataTag(entry.getValue());
                    break;
                case DataTagConstants.PRIORITY_MEDIUM:
                    this.medDynamicTimeDeadbandFilterActivator.addDataTag(entry.getValue());
                    break;
                default:
                    // other priorities are ignored
                }
            }
        }
    }

    /**
     * @param equipmentLoggerFactory the equipmentLoggerFactory to set
     */
    private void setEquipmentLoggerFactory(final EquipmentLoggerFactory equipmentLoggerFactory) {
        this.equipmentLoggerFactory = equipmentLoggerFactory;
        this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());
    }

    /**
     * Sends all through timedeadband delayed values immediately
     */
    @Override
    public void sendDelayedTimeDeadbandValues() {
        this.equipmentLogger.debug("sendDelayedTimeDeadbandValues - Sending all time deadband delayed values to the server");
        
        this.equipmentSenderValid.sendDelayedTimeDeadbandValues();
    }
    
    /**
     * Gets a source data tag with the provided id.
     * 
     * @param tagID The id of the tag to get.
     * @return The SourceDataTag with this id.
     */
    private SourceDataTag getTag(final long tagID) {
        return (SourceDataTag)this.equipmentConfiguration.getSourceDataTag(tagID);
    }
    
    /**
     * 
     * @return equipmentSenderValid
     */
    protected EquipmentSenderValid getEquipmentSenderValid() {
    	return this.equipmentSenderValid;
    }
    
    /**
     * Reconfiguration functions Add/Remove/Update
     */
    
    /**
     * Adds a data tag to this sender.
     * 
     * @param sourceDataTag The data tag to add.
     * @param changeReport The change report to fill with the results of the change.
     */
    @Override
    public void onAddDataTag(final SourceDataTag sourceDataTag, final ChangeReport changeReport) {
        DataTagAddress address = sourceDataTag.getAddress();
        if (!address.isStaticTimedeadband() && this.equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
            switch (address.getPriority()) {
            case DataTagConstants.PRIORITY_LOW:
                this.lowDynamicTimeDeadbandFilterActivator.addDataTag(sourceDataTag);
                changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " added to low priority filter.");
                break;
            case DataTagConstants.PRIORITY_MEDIUM:
                this.medDynamicTimeDeadbandFilterActivator.addDataTag(sourceDataTag);
                changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " added to medium priority filter.");
                break;
            default:
                changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " not added to any filter.");
            }
        }
    }

    /**
     * Removes a data tag from this sender.
     * 
     * @param sourceDataTag The data tag to remove.
     * @param changeReport The change report to fill with the results of the change.
     */
    @Override
    public void onRemoveDataTag(final SourceDataTag sourceDataTag, final ChangeReport changeReport) {
        this.medDynamicTimeDeadbandFilterActivator.removeDataTag(sourceDataTag);
        this.lowDynamicTimeDeadbandFilterActivator.removeDataTag(sourceDataTag);
        changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " removed from any filters.");
    }

    /**
     * Updates a data tag of this sender.
     * 
     * @param sourceDataTag The data tag to update.
     * @param oldSourceDataTag The old source data tag to identify if necessary for changes.
     * @param changeReport The change report to fill with the results.
     */
    @Override
    public void onUpdateDataTag(final SourceDataTag sourceDataTag, final SourceDataTag oldSourceDataTag,
            final ChangeReport changeReport) {
        if (!sourceDataTag.getAddress().isStaticTimedeadband()
                && sourceDataTag.getAddress().getPriority() != oldSourceDataTag.getAddress().getPriority()) {
            onRemoveDataTag(sourceDataTag, changeReport);
            onAddDataTag(sourceDataTag, changeReport);
        }
    }
}
