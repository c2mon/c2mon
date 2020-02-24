package cern.c2mon.cache.actions.datatag;

import cern.c2mon.cache.actions.tag.TagController;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.SourceDataTagValue;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Timestamp;

import static cern.c2mon.cache.actions.datatag.QualityConverter.convert;
import static cern.c2mon.cache.actions.tag.TagController.setValue;
import static cern.c2mon.shared.common.type.TypeConverter.isKnownClass;

@Slf4j
public class DataTagController {

  private DataTagController() {
  }

  public static void setValidation(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {
    if (sourceDataTagValue.getValue() == null) {
      if (sourceDataTagValue.isValid()) {
        log.debug("Null value received from source for datatag " + sourceDataTagValue.getId() + " - invalidating with quality UNKNOWN_REASON");
        invalidateWithoutValue(dataTag, TagQualityStatus.UNKNOWN_REASON, "Null value received from DAQ");
      } else {
        ((DataTagCacheObject) dataTag).setDataTagQuality(convert(sourceDataTagValue.getQuality()));
      }
    } else {
      if (sourceDataTagValue.isValid()) {
        validateWithValue(dataTag, sourceDataTagValue);
      } else {
        invalidateWithValue(dataTag, sourceDataTagValue);
      }
    }
  }

  public static void validateWithValue(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {
    setValue(dataTag, sourceDataTagValue.getValue(), sourceDataTagValue.getValueDescription());
    setTimestamps(dataTag, sourceDataTagValue.getTimestamp(), sourceDataTagValue.getDaqTimestamp());
    TagController.validate(dataTag);
  }

  public static void invalidateWithValue(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {
    setValue(dataTag, sourceDataTagValue.getValue(), sourceDataTagValue.getValueDescription());
    setTimestamps(dataTag, sourceDataTagValue.getTimestamp(), sourceDataTagValue.getDaqTimestamp());
    ((DataTagCacheObject) dataTag).setDataTagQuality(convert(sourceDataTagValue.getQuality()));
  }

  /**
   * @param dataTag     The tag which shall be invalidated
   * @param statusToAdd The quality status to be added to the data tag
   * @param description The description of the change of quality
   */
  public static void invalidateWithoutValue(DataTag dataTag, TagQualityStatus statusToAdd, String description) {
    setTimestamps(dataTag, null, null);
    dataTag.getDataTagQuality().addInvalidStatus(statusToAdd, description);
  }

  public static void setTimestamps(DataTag dataTag, Timestamp sourceTimestamp, Timestamp daqTimestamp) {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) dataTag;
    dataTagCacheObject.setSourceTimestamp(sourceTimestamp);
    dataTagCacheObject.setDaqTimestamp(daqTimestamp);
  }

  /**
   * Before updating the new value to the cache convert the value to the proper type.
   * In the process of the deserialization the dataType can still divert from the defined dataType.
   * If the dataType is an arbitrary object do nothing because the server don't work with this kind of values at all.
   *
   * @return Object, the casted value, or null
   */
  public static Object castSourceDataTagValue(DataTag dataTag, SourceDataTagValue sourceDataTagValue) {
    return sourceDataTagValue != null
      && sourceDataTagValue.getValue() != null
      && isKnownClass(dataTag.getDataType())
      ? TypeConverter.cast(sourceDataTagValue.getValue(), dataTag.getDataType())
      : null;
  }

  /**
   * Public method returning the configuration XML string for a given {@link DataTagCacheObject}
   * (was previously static in SourceDataTag class). Currently used for DAQ start up: TODO switch to generateSourceDataTag method.
   *
   * @param dataTag the cache object
   * @return the XML string
   */
  public static String generateSourceXML(final DataTag dataTag) {
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) dataTag;
    StringBuilder str = new StringBuilder("    <DataTag id=\"");

    str.append(dataTagCacheObject.getId());
    str.append("\" name=\"");
    str.append(dataTagCacheObject.getName());
    str.append("\" control=\"false\">\n");

    // <mode> ... </mode>
    if (!dataTagCacheObject.isInOperation()) {
      str.append("      <mode>");
      str.append(dataTagCacheObject.getMode());
      str.append("</mode>\n");
    }

    // <data-type> ... </data-type>
    str.append("      <data-type>");
    str.append(dataTagCacheObject.getDataType());
    str.append("</data-type>\n");

    if (dataTagCacheObject.getMinValue() != null) {
      str.append("        <min-value data-type=\"");
      str.append(dataTagCacheObject.getMinValue().getClass().getName().substring(10));
      str.append("\">");
      str.append(dataTagCacheObject.getMinValue());
      str.append("</min-value>\n");
    }

    if (dataTagCacheObject.getMaxValue() != null) {
      str.append("        <max-value data-type=\"");
      str.append(dataTagCacheObject.getMaxValue().getClass().getName().substring(10));
      str.append("\">");
      str.append(dataTagCacheObject.getMaxValue());
      str.append("</max-value>\n");
    }


    // <HardwareAddress> ... </HardwareAddress>
    if (dataTagCacheObject.getAddress() != null) {
      str.append(dataTagCacheObject.getAddress().toConfigXML());
    }

    str.append("    </DataTag>\n");
    return str.toString();
  }
}
