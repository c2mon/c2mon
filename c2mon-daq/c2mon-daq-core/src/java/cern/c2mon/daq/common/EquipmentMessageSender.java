/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005 - 2010 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.daq.common;

import static java.lang.String.format;

import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import cern.c2mon.daq.common.conf.core.EquipmentConfiguration;
import cern.c2mon.daq.common.conf.equipment.ICoreDataTagChanger;
import cern.c2mon.daq.common.messaging.IProcessMessageSender;
import cern.c2mon.daq.filter.IFilterMessageSender;
import cern.c2mon.daq.filter.dynamic.IDynamicTimeDeadbandFilterActivator;
import cern.c2mon.daq.tools.DataTagValueChecker;
import cern.c2mon.daq.tools.TIMDriverSimpleTypeConverter;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagConstants;
import cern.tim.shared.common.type.TypeConverter;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;
import cern.tim.shared.daq.filter.FilteredDataTagValue;

/**
 * EquipmentMessageSender to control all filtering and sending.
 * 
 * @author Andreas Lang
 */
public class EquipmentMessageSender implements ICoreDataTagChanger, IEquipmentMessageSender {
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
     * The timedeadband schedulers hold tags which have time deadband scheduling activated.
     */
    private Hashtable<Long, SDTTimeDeadbandScheduler> sdtTimeDeadbandSchedulers = new Hashtable<Long, SDTTimeDeadbandScheduler>();

    /**
     * The equipment configuration of this sender.
     */
    private EquipmentConfiguration equipmentConfiguration;
    /**
     * Checker to perform different types of validation and filter checks around the data tag.
     */
    private DataTagValueChecker dataTagValueChecker;
    /**
     * This is the time deadband scheduler timer where all schedulers are scheduled on.
     */
    private static Timer timeDeadbandTimer = new Timer("Time deadband timer", true);

    private Map<Long, Long> equipmentAlives = new HashMap<Long, Long>();

    /**
     * maximum allowed length of the tag quality description (will be truncated to this if too long)
     */
    private static final int MAX_QUALITY_DESC_LENGHT = 300;

    /**
     * Invalid XML chars that should not be send to the server
     */
    private static final Pattern INVALID_XML_CHARS = Pattern
            .compile("[^\\u0009\\u000A\\u000D\\u0020-\\uD7FF\\uE000-\\uFFFD\uD800\uDC00-\uDBFF\uDFFF]");

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
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     */
    public void sendSupervisionAlive() {
        sendSupervisionAlive(System.currentTimeMillis());
    }

    /**
     * This method should be invoked each time you want to propagate the supervision alive coming from the supervised
     * equipment.
     * 
     * @param milisecTimestamp the timestamp (in milliseconds)
     */
    public void sendSupervisionAlive(final long milisecTimestamp) {
        Long aliveId = Long.valueOf(equipmentConfiguration.getAliveTagId());
        SourceDataTag supAliveTag = getTag(aliveId);
        SourceDataTagValue supAliveValue;
        if (supAliveTag != null) {
            Object value = null;
            if (supAliveTag.getDataType().equalsIgnoreCase("Long")) {
                value = TypeConverter.cast(Long.valueOf(milisecTimestamp).toString(), supAliveTag.getDataType());
            } else if (supAliveTag.getDataType().equalsIgnoreCase("Integer")) {
                value = TypeConverter.cast(Long.valueOf(milisecTimestamp % Integer.MAX_VALUE).toString(),
                        supAliveTag.getDataType());
            } else {
                equipmentLogger
                        .warn("sendSupervisionAlive() - Equipment alive value is neither of type Long nor of Integer => value set to null!");
            }

            supAliveValue = supAliveTag.update(value,
                    "Equipment alive tag value has been overwritten by the DAQ Core with the source timestamp",
                    new Timestamp(milisecTimestamp));
        } else {
            supAliveValue = new SourceDataTagValue(aliveId, "eqalive", true, milisecTimestamp, null, milisecTimestamp,
                    DataTagConstants.PRIORITY_HIGH, false, null, DataTagConstants.TTL_FOREVER);
        }

        equipmentLogger.debug("sendSupervisionAlive() - Sending equipment alive message with timestamp "
                + milisecTimestamp);
        // invoke ProcessMessageSender's addValue
        processMessageSender.addValue(supAliveValue);
    }

    /**
     * Tries to send a new value to the server.
     * 
     * @param currentTag The tag to which the value belongs.
     * @param milisecTimestamp The timestamp of the tag.
     * @param tagValue The tag value to send.
     * @return True if the tag has been send successfully to the server.
     */
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
    public boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            String pValueDescr, boolean sentByValueCheckMonitor) {

        equipmentLogger.trace("entering sendTagFiltered()");

        boolean successfulSent = false;
        long tagID = currentTag.getId();
        SourceDataTag tag = getTag(tagID);

        // If we received an update of equipment alive tag, we send immediately a message to the server
        if (equipmentConfiguration.getAliveTagId() == tagID) {

            boolean preventToFrequentEquipmentAlives = Boolean.getBoolean("c2mon.daq.equipment.alive.filtering");

            if (preventToFrequentEquipmentAlives) {

                boolean sendEquipmentAlive = true;
                if (equipmentAlives.containsKey(equipmentConfiguration.getId())) {
                    long lastEquipmentAliveTimestamp = equipmentAlives.get(equipmentConfiguration.getId());

                    // if the time difference between the last eq. heartbeat and the current one is at least half of the
                    // eq. alive interval defined
                    long diff = milisecTimestamp - lastEquipmentAliveTimestamp;
                    long halfTime = Math.round(equipmentConfiguration.getAliveTagInterval() / 2.0);

                    if (diff < halfTime) {
                        if (equipmentLogger.isDebugEnabled()) {
                            equipmentLogger
                                    .debug(format(
                                            "this EquipmentAlive of equipment %s will be skipped and will not be sent the server due to enabled equipment alive filtering policy",
                                            equipmentConfiguration.getName()));
                        }

                        sendEquipmentAlive = false;
                    }
                }

                if (sendEquipmentAlive) {
                    sendSupervisionAlive(milisecTimestamp);
                    equipmentAlives.put(equipmentConfiguration.getId(), milisecTimestamp);
                    successfulSent = true;
                }

            } // if preventToFrequentEquipmentAlives

            else { // if preventToFrequentEquipmentAlives is disabled (by default it is!)

                sendSupervisionAlive(milisecTimestamp);
                successfulSent = true;
            }

         // Remove tags with invalid timestamps
        } else if (!dataTagValueChecker.isTimestampValid(milisecTimestamp)) {
            equipmentLogger
                    .warn(format(
                            "\tdeadband filtering : the timestamp of tag[%d] is out of range (in the future) and will not be propagated to the server",
                            tagID));

            equipmentLogger.debug(format("\tinvalidating tag [%d] with quality FUTURE_SOURCE_TIMESTAMP", tagID));

            // Get the source data quality from the quality code and description
            SourceDataQuality newSDQuality = createTagQualityObject(SourceDataQuality.FUTURE_SOURCE_TIMESTAMP, 
                "Value received with source timestamp in the future! Time on server was: " + new Timestamp(System.currentTimeMillis()));
            
            // Send Invalid Tag
            sendInvalidTag(currentTag, tagValue, pValueDescr, newSDQuality, new Timestamp(milisecTimestamp));
            
            // if tag has value checker monitor registered
        } else if (tag.hasValueCheckMonitor() && !sentByValueCheckMonitor) {

            if (tagValue instanceof Number) {
                ValueChangeMonitorEngine.getInstance().sendEvent(
                        new ValueChangeMonitorEvent(tag.getId(), ((Number) tagValue).doubleValue(), pValueDescr,
                                milisecTimestamp));
            } else if (tagValue instanceof Boolean) {
                Boolean v = (Boolean) tagValue;
                ValueChangeMonitorEngine.getInstance().sendEvent(
                        new ValueChangeMonitorEvent(tag.getId(), v.booleanValue() == true ? 1 : 0, pValueDescr,
                                milisecTimestamp));
            } else {
                // for strings the value is not important - we can anyway just monitor if the events arrive in regular
                // time-windows
                ValueChangeMonitorEngine.getInstance()
                        .sendEvent(
                                new ValueChangeMonitorEvent(tag.getId(), 0, pValueDescr,
                                        milisecTimestamp));
            }

            // Remove tags which have not convertable values
        } else if (!dataTagValueChecker.isConvertable(tag, tagValue)) {
            String descr = format(
                    "dataTagValueChecker : The value (%s) received for tag[%d] and the DataTag's type are not compatible.",
                    tagValue, tagID);

            equipmentLogger.warn(descr);
            equipmentLogger.debug(format("\tinvalidating tag[%d] with quality CONVERSION_ERROR", tagID));

            sendInvalidTag(tag, SourceDataQuality.CONVERSION_ERROR, descr, new Timestamp(milisecTimestamp));

            // Remove tags which are out of their range
        } else if (!dataTagValueChecker.isInRange(tag, tagValue)) {
            equipmentLogger
                    .warn(format(
                            "\tdeadband filtering : the value of tag[%d] was out of range and will only be propagated the first time to the server",
                            tagID));
            equipmentLogger.debug(format("\tinvalidating tag[%d] with quality OUT_OF_BOUNDS", tagID));

            StringBuffer qDesc = new StringBuffer("source value is out of bounds (");
            if (tag.getMinValue() != null)
                qDesc.append("min: ").append(tag.getMinValue()).append(" ");
            if (tag.getMaxValue() != null)
                qDesc.append("max: ").append(tag.getMaxValue());
            qDesc.append(")! No further updates will be processed and the tag's value will stay unchanged, until this problem is fixed");

            // Get the source data quality from the quality code and description
            SourceDataQuality newSDQuality = createTagQualityObject(SourceDataQuality.OUT_OF_BOUNDS, qDesc.toString());
            
            // Send Invalid Tag
            sendInvalidTag(currentTag, tagValue, pValueDescr, newSDQuality, new Timestamp(milisecTimestamp));

            // Filter tags through value deadband filtering
        } else if (dataTagValueChecker.isValueDeadbandFiltered(tag, tagValue, pValueDescr)) {
            equipmentLogger
                    .debug(format(
                            "\tvalue-deadband filtering : the value of tag [%d] was filtered out due to value-deadband filtering rules and will not be sent to the server",
                            tagID));

            equipmentLogger.debug("sending value to statistics module");
            sendToFilterModule(currentTag, tagValue, milisecTimestamp, pValueDescr, false,
                    FilteredDataTagValue.VALUE_DEADBAND);
            
            // Filter tags which didn't change their value
        } else if (dataTagValueChecker.isSameValue(tag, tagValue, pValueDescr)) {
            equipmentLogger
                    .debug(format(
                            "\ttrying to send twice the same tag [%d] update (with exactly the same value and value description). Rejecting by default",
                            tagID));
            // send this value to the statistics module
            equipmentLogger.debug("sending the value to the statistics module");
            sendToFilterModule(currentTag, tagValue, milisecTimestamp, pValueDescr, false,
                    FilteredDataTagValue.REPEATED_VALUE);

        } else if (tag.getAddress().isTimeDeadbandEnabled()) {
            // get the reference to the time deadband scheduler
            addToTimeDeadband(tag, tagValue, milisecTimestamp, pValueDescr);
        } else {
            if (sdtTimeDeadbandSchedulers.containsKey(tagID))
                removeFromTimeDeadband(tag);

            // All checks and filters are done
            sendTag(tagValue, milisecTimestamp, pValueDescr, tag);
            recordTag(tag);
            successfulSent = true;
        }

        equipmentLogger.trace("leaving sendTagFiltered()");
        return successfulSent;
    }

    /**
     * Updates the tag value and sends it. This method should be only used in core.
     * 
     * @param tagValue The new value of the tag.
     * @param milisecTimestamp The timestamp to use.
     * @param pValueDescr The description of the value.
     * @param tag The tag to update.
     */
    public void sendTag(final Object tagValue, final long milisecTimestamp, final String pValueDescr,
            final SourceDataTag tag) {
        processMessageSender.addValue(tag.update(convertValue(tag, tagValue), pValueDescr, new Timestamp(
                milisecTimestamp)));
    }

    /**
     * Converts the input value to the expected type
     * 
     * @param tag
     * @param inValue
     * @return
     */
    private Object convertValue(ISourceDataTag tag, Object inValue) {
        Object outValue = null;
        if (inValue instanceof Number) {
            outValue = TIMDriverSimpleTypeConverter.convert(tag, (Number) inValue);
        } else if (inValue instanceof Boolean) {
            outValue = inValue;
        } else {
            outValue = inValue.toString();
        }

        return outValue;
    }

    /**
     * Gets a source data tag with the provided id.
     * 
     * @param tagID The id of the tag to get.
     * @return The SourceDataTag with this id.
     */
    private SourceDataTag getTag(final long tagID) {
        return equipmentConfiguration.getDataTags().get(tagID);
    }

    /**
     * Gets a source data tag matching the provided ISourceDataTag
     * 
     * @param sourceDataTag The ISourceDataTag to get the matching SourceDataTag.
     * @return The matching SourceDataTag.
     */
    private SourceDataTag getTag(final ISourceDataTag sourceDataTag) {
        return getTag(sourceDataTag.getId());
    }

    /**
     * Starts the time deadband scheduler for this tag.
     * 
     * @param currentTag The tag which should have a time deadband scheduler.
     */
    private void startSDTtimeDeadbandScheduler(final SourceDataTag currentTag) {
        if (currentTag.getAddress().isTimeDeadbandEnabled()) {
            if (currentTag.getAddress().getTimeDeadband() > 0) {
                sdtTimeDeadbandSchedulers.put(currentTag.getId(), new SDTTimeDeadbandScheduler(currentTag, this,
                        timeDeadbandTimer, dataTagValueChecker));
            }
        }
    }

    /**
     * Sends a message to the statistics module. Should only be used in the core.
     * 
     * @param currentTag The tag to send.
     * @param quality The quality of the tag.
     * @param tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param dynamicFiltered True if the tag was dynamic filtered.
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final ISourceDataTag currentTag, final SourceDataQuality quality,
            final Object tagValue, final long milisecTimestamp, final String pValueDescr,
            final boolean dynamicFiltered, final short filterType) {
        filterMessageSender.addValue(getTag(currentTag).makeFilterValue(quality, new Timestamp(milisecTimestamp),
                dynamicFiltered, filterType));
    }

    /**
     * Sends a message to the filter log. Should only be used in the core.
     * 
     * @param sourceDataTag The tag to send.
     * @param tagValue tagValue The value of the tag.
     * @param milisecTimestamp The timestamp in ms.
     * @param pValueDescr A description of the value (optional)
     * @param dynamicFiltered True if the tag was dynamic filtered.
     * @param filterType The type of the applied filter, see {@link FilteredDataTagValue} constants
     */
    public void sendToFilterModule(final ISourceDataTag sourceDataTag, final Object tagValue,
            final long milisecTimestamp, final String pValueDescr, final boolean dynamicFiltered, final short filterType) {
        filterMessageSender.addValue(getTag(sourceDataTag).makeFilterValue(new Timestamp(milisecTimestamp), tagValue,
                pValueDescr, dynamicFiltered, filterType));
    }

    /**
     * Sends the CommfaultTag message.
     * 
     * @param tagID The CommfaultTag id.
     * @param value The CommFaultTag value to send.
     * @param description The description of the CommfaultTag
     */
    private void sendCommfaultTag(final long tagID, final Boolean value, final String description) {
        if (equipmentLogger.isDebugEnabled()) {
            equipmentLogger.debug("entering sendCommfaultTag()..");
            equipmentLogger.debug("\tCommFaultTag: #" + tagID);
        }
        if (description == null) {
            getProcessMessageSender().sendCommfaultTag(tagID, value);
        } else {
            getProcessMessageSender().sendCommfaultTag(tagID, value, description);
        }
        equipmentLogger.debug("leaving sendCommfaultTag()");
    }
    
    /**
     * Creates a new quality object for a {@link SourceDataTag}
     * 
     * @param pQualityCode A quality code which is defined as constant in SourceDataQuality
     * @param pDescription A quality description
     * 
     * @return A new instance of {@link SourceDataQuality}
     */
    public SourceDataQuality createTagQualityObject(final short pQualityCode, final String pDescription) {
      SourceDataQuality sdQuality;
      if (pDescription == null) {
          sdQuality = new SourceDataQuality(pQualityCode);
      } else {
          String pQualityDesc;
          if (pDescription.length() > MAX_QUALITY_DESC_LENGHT) {
              pQualityDesc = pDescription.substring(0, MAX_QUALITY_DESC_LENGHT - 1);
          } else {
              pQualityDesc = pDescription;
          }

          // Strip out all invalid XML characters
          pQualityDesc = INVALID_XML_CHARS.matcher(pQualityDesc).replaceAll("");

          sdQuality = new SourceDataQuality(pQualityCode, pQualityDesc);
      }
      
      return sdQuality;
    }

    /**
     * This method sends an invalid SourceDataTagValue to the server. Source and DAQ timestamps are set to the current
     * DAQ system time.
     * 
     * @param sourceDataTag SourceDataTag object
     * @param pQualityCode the SourceDataTag's quality see {@link SourceDataQuality} class for details
     * @param pDescription the quality description (optional)
     */
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
      SourceDataQuality newSDQuality = createTagQualityObject(qualityCode, qualityDescription);
      
      // The sendInvalidTag function with the value argument will take are of it
      if (sourceDataTag.getCurrentValue() != null) {
        sendInvalidTag(sourceDataTag, sourceDataTag.getCurrentValue().getValue(), sourceDataTag.getCurrentValue().getValueDescription(), 
            newSDQuality, pTimestamp);
      }
      else {
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

      // Create time stamp for filterng or invalidating
      Timestamp timestamp;
      if (pTimestamp == null) {
        timestamp = new Timestamp(System.currentTimeMillis());
      } else {
        timestamp = pTimestamp;
      }

      long tagID = sourceDataTag.getId();
      SourceDataTag tag = getTag(tagID);

      try {
        // We check first is the new value has to be filtered out or not
        if (this.dataTagValueChecker.isCandidateForFiltering(tag, newValue, newTagValueDesc, newSDQuality)) {
          // If we are here the new Value will be filtered out
          if (this.equipmentLogger.isDebugEnabled()) {
            StringBuilder msgBuf = new StringBuilder();
            msgBuf.append("\tthe tag : " + tagID
                + " has already been invalidated with quality code : " + newSDQuality.getQualityCode());
            msgBuf.append(" at " + tag.getCurrentValue().getTimestamp());
            msgBuf.append(" The DAQ has not received any values with different quality since than, Hence, the");
            msgBuf.append(" invalidation procedure will be canceled this time");
            this.equipmentLogger.debug(msgBuf.toString());
          }

          /*
           * the value object can be null if several invalid data tags are sent when the DAQ is started up (the
           * value object is still null, but the currentValue object is not anymore) in this case, we choose not
           * to send it to the filter path
           */
          if (newValue != null) {
            // send a corresponding INVALID tag to the statistics module
            this.equipmentLogger.debug("sendInvalidTag - sending an invalid tag to the statistics module");

            // send filtered message to statistics module
            sendToFilterModule(tag, newSDQuality, newValue, timestamp.getTime(), newTagValueDesc, false,
                FilteredDataTagValue.REPEATED_INVALID);

          } else if (this.equipmentLogger.isDebugEnabled()) {
            this.equipmentLogger.debug("sendInvalidTag - value has still not been initialised: not sending the invalid tag to the statistics module");
          }
        } else {
          // If we are here the new value will not be filtered out
          
          // If time deadband is enabled for that tag stop it
          if (tag.getAddress().isTimeDeadbandEnabled()) {
            this.equipmentLogger.debug("sendInvalidTag - flush and reset time-deadband scheduler for tag " + tagID);
            flushAndResetTimeDeadband(tag);
          }

          this.equipmentLogger.debug(format("sendInvalidTag - invalidating and sending invalid tag (%d) update to the server", tagID));
          
          SourceDataTagValue newSDValue = tag.invalidate(newSDQuality, newValue, newTagValueDesc, timestamp);
          // Special case Quality OK     
          if (newSDValue == null) {
            // this means we have a valid quality code 0 (OK)
            equipmentLogger.warn("sendInvalidTag - method called with 0(OK) quality code for tag "                                         + tagID
                + ". This should normally not happen! Redirecting call to sendTagFiltered() method.");
            sendTagFiltered(tag, newValue, timestamp.getTime(), newTagValueDesc);
          }
          else {
            // All checks and filters are done
            this.processMessageSender.addValue(newSDValue);
            recordTag(tag);
          }
        }
      } catch (Exception ex) {
        this.equipmentLogger.error("\tsendInvalidTag - Unexpected exception caught !", ex);
      }
      this.equipmentLogger.debug("sendInvalidTag - leaving sendInvalidTag()");
    }

    /**
     * Adds the provided tag value to the tagScheduler of this tag.
     * 
     * @param currentTag The tag of which the tag scheduler should be used.
     * @param tagValue The value of the tag.
     * @param milisecTimestamp A timestamp in ms.
     * @param pValueDescr An optional value description.
     */
    private void addToTimeDeadband(final SourceDataTag currentTag, final Object tagValue, final long milisecTimestamp,
            final String pValueDescr) {
        long tagID = currentTag.getId();
        SDTTimeDeadbandScheduler tagScheduler = sdtTimeDeadbandSchedulers.get(tagID);
        if (tagScheduler == null) {
            startSDTtimeDeadbandScheduler(currentTag);
            tagScheduler = sdtTimeDeadbandSchedulers.get(tagID);
        }

        recordTag(currentTag);

        // if the scheduler is set to send the current tag value,
        // then we need to send it
        // to the statistics module before updating the tag:
        if (tagScheduler.isScheduledForSending()) {
            equipmentLogger.debug("Sending time deadband filtered value to statistics module " + tagID);
            boolean dynamicFiltered = !currentTag.getAddress().isStaticTimedeadband();
            equipmentLogger.debug("Tag filtered through time deadband filtering: '" + tagID + "'");
            sendToFilterModule(currentTag, currentTag.getCurrentValue().getValue(), currentTag.getCurrentValue()
                    .getTimestamp().getTime(), currentTag.getCurrentValue().getValueDescription(), dynamicFiltered,
                    FilteredDataTagValue.TIME_DEADBAND);
        }

        // update the tag value
        currentTag.update(tagValue, pValueDescr, new Timestamp(milisecTimestamp));

        equipmentLogger.debug("scheduling value update due to time-deadband filtering rule");
        // notify the scheduler that it contains a value that needs sending
        tagScheduler.scheduleValueForSending();
    }

    /**
     * Stops the time deadband scheduler of this tag and removes it from the map of schedulers.
     * 
     * @param currentTag The tag to remove.
     */
    private void removeFromTimeDeadband(final SourceDataTag currentTag) {
        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("entering removeFromTimeDeadband(%d)..", currentTag.getId()));
        SDTTimeDeadbandScheduler scheduler = sdtTimeDeadbandSchedulers.remove(currentTag.getId());
        if (scheduler != null) {
            equipmentLogger.trace("\tcancelling scheduler");
            scheduler.cancel();
            if (scheduler.isScheduledForSending())
                equipmentLogger
                        .trace("\tforcing scheduler to run its run() in order to send the flush buffered message (if any)");
            scheduler.run();
        }

        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("leaving removeFromTimeDeadband(%d)", currentTag.getId()));
    }

    /**
     * Flushes and resets the scheduler of this tag
     * 
     * @param currentTag The tag
     */
    private void flushAndResetTimeDeadband(final SourceDataTag currentTag) {
        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("entering flushAndResetTimeDeadband(%d)..", currentTag.getId()));
        SDTTimeDeadbandScheduler scheduler = sdtTimeDeadbandSchedulers.get(currentTag.getId());
        if (scheduler != null) {
            scheduler.flushAndReset();
        }

        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("leaving flushAndResetTimeDeadband(%d)", currentTag.getId()));
    }

    /**
     * Depending on the tag priority it will be recorded for dynamic time deadband filtering.
     * 
     * @param tag The tag to be recorded.
     */
    private void recordTag(final SourceDataTag tag) {
        DataTagAddress address = tag.getAddress();
        if (!address.isStaticTimedeadband() && equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
            switch (address.getPriority()) {
            case DataTagConstants.PRIORITY_LOW:
                lowDynamicTimeDeadbandFilterActivator.newTagValueSent(tag.getId());
                break;
            case DataTagConstants.PRIORITY_MEDIUM:
                medDynamicTimeDeadbandFilterActivator.newTagValueSent(tag.getId());
                break;
            default:
                // other priorities are ignored
                break;
            }
        }
    }

    /**
     * Returns the process message sender.
     * 
     * @return The process message sender to be returned.
     */
    private IProcessMessageSender getProcessMessageSender() {
        return processMessageSender;
    }

    /**
     * Sets the equipment configuration
     * 
     * @param equipmentConfiguration The equipment configuration.
     */
    public void setEquipmentConfiguration(final EquipmentConfiguration equipmentConfiguration) {
        this.equipmentConfiguration = equipmentConfiguration;
        Map<Long, SourceDataTag> sourceDataTags = equipmentConfiguration.getDataTags();
        medDynamicTimeDeadbandFilterActivator.clearDataTags();
        lowDynamicTimeDeadbandFilterActivator.clearDataTags();
        for (Entry<Long, SourceDataTag> entry : sourceDataTags.entrySet()) {
            DataTagAddress address = entry.getValue().getAddress();
            if (!address.isStaticTimedeadband() && equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
                switch (address.getPriority()) {
                case DataTagConstants.PRIORITY_LOW:
                    lowDynamicTimeDeadbandFilterActivator.addDataTag(entry.getValue());
                    break;
                case DataTagConstants.PRIORITY_MEDIUM:
                    medDynamicTimeDeadbandFilterActivator.addDataTag(entry.getValue());
                    break;
                default:
                    // other priorities are ignored
                }
            }
        }
    }

    /**
     * Adds a data tag to this sender.
     * 
     * @param sourceDataTag The data tag to add.
     * @param changeReport The change report to fill with the results of the change.
     */
    @Override
    public void onAddDataTag(final SourceDataTag sourceDataTag, final ChangeReport changeReport) {
        DataTagAddress address = sourceDataTag.getAddress();
        if (!address.isStaticTimedeadband() && equipmentConfiguration.isDynamicTimeDeadbandEnabled()) {
            switch (address.getPriority()) {
            case DataTagConstants.PRIORITY_LOW:
                lowDynamicTimeDeadbandFilterActivator.addDataTag(sourceDataTag);
                changeReport.appendInfo("Data tag " + sourceDataTag.getId() + " added to low priority filter.");
                break;
            case DataTagConstants.PRIORITY_MEDIUM:
                medDynamicTimeDeadbandFilterActivator.addDataTag(sourceDataTag);
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
        medDynamicTimeDeadbandFilterActivator.removeDataTag(sourceDataTag);
        lowDynamicTimeDeadbandFilterActivator.removeDataTag(sourceDataTag);
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

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     */
    public final void confirmEquipmentStateIncorrect() {
        confirmEquipmentStateIncorrect(null);
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is not properly configured, or connected to its
     * data source
     * 
     * @param pDescription additional description
     */
    public final void confirmEquipmentStateIncorrect(final String pDescription) {
        sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), equipmentConfiguration.getCommFaultTagValue(),
                pDescription);
        Enumeration<Long> enume = equipmentConfiguration.getSubEqCommFaultValues().keys();
        // Send the commFaultTag for the equipment's subequipments too
        if (enume != null) {
            while (enume.hasMoreElements()) {
                Long commFaultId = enume.nextElement();
                sendCommfaultTag(commFaultId, equipmentConfiguration.getSubEqCommFaultValues().get(commFaultId),
                        pDescription);
            }
        }
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     */
    public final void confirmEquipmentStateOK() {
        confirmEquipmentStateOK(null);
    }

    /**
     * Sends a note to the business layer, to confirm that the equipment is properly configured, connected to its source
     * and running
     * 
     * @param pDescription additional description
     */
    public final void confirmEquipmentStateOK(final String pDescription) {
        sendCommfaultTag(equipmentConfiguration.getCommFaultTagId(), !equipmentConfiguration.getCommFaultTagValue(),
                pDescription);
        Enumeration<Long> enume = equipmentConfiguration.getSubEqCommFaultValues().keys();
        // Send the commFaultTag for the equipment's subequipments too
        if (enume != null) {
            while (enume.hasMoreElements()) {
                Long commFaultId = enume.nextElement();
                sendCommfaultTag(commFaultId, !(equipmentConfiguration.getSubEqCommFaultValues().get(commFaultId)),
                        pDescription);
            }
        }
    }

    /**
     * @param equipmentLoggerFactory the equipmentLoggerFactory to set
     */
    public void setEquipmentLoggerFactory(final EquipmentLoggerFactory equipmentLoggerFactory) {
        this.equipmentLoggerFactory = equipmentLoggerFactory;
        equipmentLogger = this.equipmentLoggerFactory.getEquipmentLogger(getClass());
        dataTagValueChecker = new DataTagValueChecker(equipmentLogger);
    }

    /**
     * Sends all through timedeadband delayed values immediately
     */
    @Override
    public void sendDelayedTimeDeadbandValues() {
        equipmentLogger.debug("Sending all time deadband delayed values to the server");
        for (SDTTimeDeadbandScheduler scheduler : sdtTimeDeadbandSchedulers.values()) {
            if (scheduler.isScheduledForSending()) {
                scheduler.run();
            }
        }
    }
}
