/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.dip;

import java.lang.reflect.Array;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import cern.c2mon.daq.common.impl.EquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.dip.DipData;
import cern.dip.DipDataUtil;
import lombok.extern.slf4j.Slf4j;


/**
 * The {@link ValueHandler} class is extracing the value from the received DIP message and
 * passes it to the {@link EquipmentMessageSender}.
 *
 * @author Matthias Braeger
 */
@Slf4j
class ValueHandler {

  /** field name used when dip primitive is sent */
  private final static String fieldNameWhenPrimitive = "__DIP_DEFAULT__";

  /** DIP controller */
  private final DIPController dipController;

  /** Jackson mapper to serialise to JSON */
  private final ObjectMapper mapper = new ObjectMapper();

  /**
   * Default Constructor
   */
  protected ValueHandler(DIPController dipController) {
    this.dipController = dipController;
  }

  /**
   * If the value is complex (structured), we take only one explicit value from the structure,
   * which is specified by the field name
   */
  public void parseMessageAndSendValue(final DipData message, final ISourceDataTag sdt, final DIPHardwareAddress dipDataTagAddress) {

    // get DipData's timestamp
    final long timestamp = message.extractDipTime().getAsMillis();
    log.trace("\ttimestamp retrieved from the DipData : " + new Timestamp(timestamp));

    try {

      Object dipValue = null; // The extracted value from the DIP message, which has to be send to the C2MON server
      String fieldName = dipDataTagAddress.getFieldName(); // might be null

      if (fieldName == null && message.getTags().length > 1) {
        log.trace("Serialising complex DIP topic as JSON string");
        dipValue = mapper.writeValueAsString(getValueAsMap(message));
      }
      else {

        // Check for primitive type subscription
        if (fieldName == null && message.getTags().length == 1) {
          fieldName = fieldNameWhenPrimitive;
        }

        // Extracting DIP value type of given field
        int valueType = message.getValueType(fieldName);

        if (valueType == DipData.TYPE_NULL) {
          log.error(String.format("\tIncorrect native address for Tag #%d. DIP field \"%s\" is unknown", sdt.getId(), fieldName));
          dipController.getEquipmentMessageSender().sendInvalidTag(sdt,
                                                                   SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                                                                   "Unknown field \"" + fieldName + "\" specified in DIP address.");
          return;
        }

        dipValue = DipDataUtil.extractDipObjectValue(valueType, message, fieldName);

        if (dipDataTagAddress.getFieldIndex() > -1) {
          dipValue = extractObjectValueFromArray(dipValue, valueType, dipDataTagAddress.getFieldIndex());
        }
      }

      sendValue(sdt, dipValue, timestamp);


    }
    catch (Exception ex) {
      log.error("\tError occured whilst extracting DIP value from received update messge for tag #" + sdt.getId(), ex);
      dipController.getEquipmentMessageSender().sendInvalidTag(sdt,
                                                               SourceDataQuality.CONVERSION_ERROR,
                                                               "Error occured whilst extracting DIP value from received update message. Reason: " + ex.getMessage());
    }
  }

  /**
   * @param message The DIP message to convert
   * @return A Map representation of the {@link DipData} message
   * @throws Exception In case of an error when extracting the nested DIP values
   */
  private Map<String, Object> getValueAsMap(final DipData message) throws Exception {
    Map<String, Object> result = new HashMap<String, Object>();

    for (String tag : message.getTags()) {
      result.put(
          tag,
          DipDataUtil.extractDipObjectValue(message.getValueType(tag), message, tag));
    }

    return result;
  }

  /**
   * Sends the new value to the server
   *
   * @param sdt the {@link SourceDataTag} that we want to update
   * @param dipValue the new value
   * @param timestamp the source timestamp
   */
  private void sendValue(ISourceDataTag sdt, Object dipValue, long timestamp) {

    if (dipController.getEquipmentMessageSender().sendTagFiltered(sdt, dipValue, timestamp)) {

      if (dipValue.getClass().isArray()) {
        log.debug("  New value (" + arrayToString(dipValue) + ") sent to the server for Tag \"" + sdt.getName() + "\" (#" + sdt.getId() + ")");
      }
      else {
        log.debug("  New value (" + dipValue + ") sent to the server for Tag \"" + sdt.getName() + "\" (#" + sdt.getId() + ")");
      }

    } else {
      sendDebugLogForUnsuccessfulSending(sdt, dipValue);
    }
  }

  /**
   * Helper method to print a primitive array
   * @param array the primitive array
   * @return String representation of the array
   */
  private String arrayToString(Object array) {
    if (array instanceof boolean[]) {
      return Arrays.toString((boolean[]) array);
    }
    if (array instanceof byte[]) {
      return Arrays.toString((byte[]) array);
    }
    if (array instanceof long[]) {
      return Arrays.toString((long[]) array);
    }
    if (array instanceof double[]) {
      return Arrays.toString((double[]) array);
    }
    if (array instanceof float[]) {
      return Arrays.toString((float[]) array);
    }
    if (array instanceof int[]) {
      return Arrays.toString((int[]) array);
    }
    if (array instanceof short[]) {
      return Arrays.toString((short[]) array);
    }
    if (array instanceof String[]) {
      return Arrays.toString((String[]) array);
    }

    return array.toString();
  }


  private Object extractObjectValueFromArray(Object dipValue, int valueType, int index) {

    switch (valueType) {

      case DipData.TYPE_BOOLEAN_ARRAY:
      case DipData.TYPE_BYTE_ARRAY:
      case DipData.TYPE_DOUBLE_ARRAY:
      case DipData.TYPE_FLOAT_ARRAY:
      case DipData.TYPE_INT_ARRAY:
      case DipData.TYPE_LONG_ARRAY:
      case DipData.TYPE_SHORT_ARRAY:
      case DipData.TYPE_STRING_ARRAY:

        return Array.get(dipValue, index);

      default:
        log.warn("extractObjectValueFromArray() : No array object given -> Returning original value");
        return dipValue;
    }
  }

  /**
   * Inner method for creating a log message that explains why the newValue could not be send to the server.
   * @param sdt SourceDataTag
   * @param newValue The new value received by DIP
   */
  private void sendDebugLogForUnsuccessfulSending(final ISourceDataTag sdt, final Object newValue) {
    log.debug("  The new value update (" + newValue + ") for Tag name : "
        + sdt.getName() + " tag id : " + sdt.getId() + " was filtered out or invalidated before sending to the server.");
  }
}
