/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.common;

import java.sql.Timestamp;

import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;

/**
 * Interface for equipment message senders.
 *
 * @author Andreas Lang
 */
public interface IEquipmentMessageSender {

  /**
   * This method should be invoked each time you want to propagate the
   * supervision alive coming from the supervised equipment.
   */
  void sendSupervisionAlive();

  /**
   * Tries to send a new value to the server.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The source timestamp of the tag in milliseconds.
   * @param tagValue The tag value to send.
   * @return True if the tag has been send successfully to the server. False if
   *         the tag has been invalidated or filtered out.
   */
  boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long sourceTimestamp);

  /**
   * Tries to send a new value to the server.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The source timestamp of the tag in milliseconds.
   * @param tagValue The tag value to send.
   * @param pValueDescr A description belonging to the value.
   * @return True if the tag has been send successfully to the server. False if
   *         the tag has been invalidated or filtered out.
   */
  boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long sourceTimestamp, final String pValueDescr);

  /**
   * Tries to send a new value to the server.
   *
   * @param currentTag The tag to which the value belongs.
   * @param sourceTimestamp The source timestamp of the tag in milliseconds.
   * @param tagValue The tag value to send.
   * @param pValueDescr A description belonging to the value.
   * @param sentByValueCheckMonitor
   * @return True if the tag has been send successfully to the server. False if
   *         the tag has been invalidated or filtered out.
   */
  boolean sendTagFiltered(final ISourceDataTag currentTag, final Object tagValue, final long sourceTimestamp, final String pValueDescr,
      boolean sentByValueCheckMonitor);

  /**
   * This method sends an invalid SourceDataTagValue to the server.
   *
   * @param sourceDataTag SourceDataTag object
   * @param pQualityCode the SourceDataTag's quality see
   *          {@link SourceDataQuality} class for details
   * @param pDescription the quality description (optional)
   */
  void sendInvalidTag(final ISourceDataTag sourceDataTag, final short pQualityCode, final String pDescription);

  /**
   * This method sends an invalid SourceDataTagValue to the server.
   *
   * @param sourceDataTag SourceDataTag object
   * @param pQualityCode the SourceDataTag's quality see
   *          {@link SourceDataQuality} class for details
   * @param pDescription the quality description (optional)
   * @param sourceTimestamp time when the SourceDataTag's value has become invalid
   */
  void sendInvalidTag(final ISourceDataTag sourceDataTag, final short pQualityCode, final String pDescription, final Timestamp sourceTimestamp);

  /**
   * Sends a note to the business layer, to confirm that the equipment is not
   * properly configured, or connected to its data source
   */
  void confirmEquipmentStateIncorrect();

  /**
   * Sends a note to the business layer, to confirm that the equipment is not
   * properly configured, or connected to its data source
   *
   * @param pDescription additional description
   */
  void confirmEquipmentStateIncorrect(final String pDescription);

  /**
   * Sends a note to the business layer, to confirm that a subequipment is not
   * properly configured, or connected to its data source
   *
   * @param commFaultTagId the ID of the subequipment commfault tag
   */
  void confirmSubEquipmentStateIncorrect(Long commFaultTagId);

  /**
   * Sends a note to the business layer, to confirm that a subequipment is not
   * properly configured, or connected to its data source
   *
   * @param commFaultTagId the ID of the subequipment commfault tag
   * @param pDescription additional description
   */
  void confirmSubEquipmentStateIncorrect(Long commFaultTagId, final String pDescription);

  /**
   * Sends a note to the business layer, to confirm that the equipment is
   * properly configured, connected to its source and running
   */
  void confirmEquipmentStateOK();

  /**
   * Sends a note to the business layer, to confirm that the equipment is
   * properly configured, connected to its source and running
   *
   * @param pDescription additional description
   */
  void confirmEquipmentStateOK(final String pDescription);

  /**
   * Sends a note to the business layer, to confirm that a subequipment is
   * properly configured, connected to its source and running
   *
   * @param commFaultTagId the ID of the subequipment commfault tag
   */
  void confirmSubEquipmentStateOK(Long commFaultTagId);

  /**
   * Sends a note to the business layer, to confirm that a subequipment is
   * properly configured, connected to its source and running
   *
   * @param commFaultTagId the ID of the subequipment commfault tag
   * @param pDescription additional description
   */
  void confirmSubEquipmentStateOK(Long commFaultTagId, final String pDescription);

  /**
   * Sends all through timedeadband delayed values immediately
   */
  void sendDelayedTimeDeadbandValues();

}
