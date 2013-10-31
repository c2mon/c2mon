package cern.c2mon.daq.tools;

import static java.lang.String.format;
import cern.c2mon.daq.common.EquipmentLogger;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.common.type.TagDataType;
import cern.tim.shared.common.type.TypeConverter;
import cern.tim.shared.daq.datatag.SourceDataQuality;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValue;

/**
 * The data tag value checker has methods to perform different types of checks around DataTags, timestamps or
 * DataTagValues. Its main purpose is to determine if a data tag is valid and not filtered.
 * 
 * @author Andreas Lang
 */
public class DataTagValueChecker {
    /**
     * The logger of this class
     */
    private EquipmentLogger equipmentLogger;
    /**
     * The maximum allowed difference between the system's timestamp and the equipment's timestamp
     */
    private static final int MAX_MSECONDS_DIFF = 300000; // 5 minutes
    /**
     * Factor to calculate from a percentage value to a simple factor.
     */
    private static final double PERCENTAGE_FACTOR = 0.01;

    /**
     * Creates a new Data Tag value checker which uses the provided equipment logger to log its results.
     * 
     * @param equipmentLogger The equipment logger to use.
     */
    public DataTagValueChecker(final EquipmentLogger equipmentLogger) {
        this.equipmentLogger = equipmentLogger;
    }

    /**
     * Checks if the current tag has the same value as the new tag Value, including the value description. Note that a
     * value description of null and empty string are considered the same here.
     * <p>
     * Returns true if tagValue parameter is null.
     * 
     * @param tag The tag to check.
     * @param tagValue The tagValue to check; can be null
     * @param valueDescr The valueDescription to check; can be null
     * @return True if they have the same value else false.
     * @throws NullPointerException if tag parameter is null
     */
    public boolean isSameValue(final SourceDataTag tag, final Object tagValue, final String valueDescr) {
        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("entering isSameValue(%d)..", tag.getId()));

        boolean isSameValue = false;
        if (isCurrentValueAvailable(tag)) {
            String tagValueDesc = tag.getCurrentValue().getValueDescription();
            if (tagValueDesc == null) {
                tagValueDesc = "";
            }
            String newValueDesc = valueDescr;
            if (newValueDesc == null) {
                newValueDesc = "";
            }
            isSameValue = tag.getCurrentValue().isValid() && tag.getCurrentValue().getValue().equals(tagValue)
                    && tagValueDesc.equalsIgnoreCase(newValueDesc);
        }

        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("leaving isSameValue(%d). Result: %b", tag.getId(), isSameValue));
        return isSameValue;
    }

    /**
     * Compares the value and quality information of the current {@link SourceDataTagValue} against the newly received 
     * quality information.
     * Avoid sending twice (one by one) 2 invalid tags with the same quality code and description
     * 
     * @param currentTag The current tag object of the {@link SourceDataTag} that shall be updated
     * @param newValue The new update value that we want set to the tag 
     * @param newTagValueDesc The new update value description
     * @param newSDQuality The new quality info for the {@link SourceDataTag} that shall be updated
     * 
     * @return <code>true</code>, if this the new quality is a candidate for being filtered out. Otherwhise <code>false</code>. 
     */
    public boolean isCandidateForFiltering(final SourceDataTag currentTag, final Object newValue, final String newTagValueDesc,
        final SourceDataQuality newSDQuality) {
      short newQualityCode = newSDQuality.getQualityCode(); 
    
      SourceDataTagValue currentSDValue = currentTag.getCurrentValue();
      if (currentSDValue != null) {
        if (currentSDValue.getValue() == null && newValue != null) {
          // Got a new value which is initializing our SourceDataTag. Hence we do not want to filter it out!
          this.equipmentLogger.trace("isCandidateForFiltering - Current Value null but we have a New value. Not candidate for filtering" 
              + currentSDValue.getId());

          return false;
        }        
        else if (currentSDValue.getValue() != null && !currentSDValue.getValue().equals(newValue)) {  
          // The two value are different, hence we do not want to filter it out ... unless the Value dead band filter said the opposite
          if (isValueDeadbandFiltered(currentTag, newValue, newTagValueDesc, newSDQuality)) {
            this.equipmentLogger.trace("isCandidateForFiltering - New value update but within value deadband filter." 
                + " Candidate for filtering" + currentSDValue.getId());
            
            return true;
          }
          
          // The two value are different, so it is clear we do not want to filter it out!
          this.equipmentLogger.trace("isCandidateForFiltering - Both Values are different. Not candidate for filtering" 
              + currentSDValue.getId());
          
          return false;
        }
        // Current and new value are both null or equal! Now we check, for redundant quality information
        else if (currentSDValue.getQuality() != null) {
          // Check, if quality code did not change
          if ((currentSDValue.getQuality().getQualityCode() == newQualityCode)) {
            this.equipmentLogger.trace("isCandidateForFiltering - Both Quality codes are equal. Check Descriptions to take a decition" 
                + currentSDValue.getId());
            // Check if quality description did not change. If it is not null we compare it with the new one
            if (currentSDValue.getQuality().getDescription() == null) {
              // If description is null we cannot compare so we check directly if both are null or not
              if (newSDQuality.getDescription() == null) {
                // We filter out since both are the same and null
                this.equipmentLogger.trace("isCandidateForFiltering - Both Descriptions are null. Candidate for filtering" 
                    + currentSDValue.getId());
               
                return true;
              }
              else {
                this.equipmentLogger.trace("isCandidateForFiltering - Current Description null but we have a New Description. " 
                    + "Not candidate for filtering" + currentSDValue.getId());
                
                // Goes directly to the final return
              }
            } 
            // Description is not null. We can compare it with the new description
            else {
              if (currentSDValue.getQuality().getDescription().equals(newSDQuality.getDescription())) {
                // If we are here, it means we have received a redundant quality code and description ==> should be filtered out.
                this.equipmentLogger.trace("isCandidateForFiltering - Both Descriptions are equal. Candidate for filtering" 
                    + currentSDValue.getId());
                
                return true;
              }
              else if (newQualityCode == SourceDataQuality.FUTURE_SOURCE_TIMESTAMP) {
                // If we are here, it means we received a new event with the same value and quality code but with a different quality description.
                // However, for FUTURE TIMESTAMPS we still want to filter out the message.
                this.equipmentLogger.trace("isCandidateForFiltering - Both Descriptions are different but special Quality code case: " 
                    + "FUTURE_SOURCE_TIMESTAMP. Candidate for filtering" + currentSDValue.getId());
                
                return true;
              }
            }
          }
          // Different Quality Codes 
          else {
            this.equipmentLogger.trace("isCandidateForFiltering - Both Quality Codes are different. Not candidate for filtering" 
                + currentSDValue.getId());
          }
        }
      } // in case the SourceDataTag value has never been initialized we don't want to filter
      
      // We got a new quality information that we want to send to the server.
      return false;
    }
    
    /**
     * Checks if there is a not null value for this tag.
     * 
     * @param tag The tag to check.
     * @return Returns true if a not null value is available else false.
     */
    private boolean isCurrentValueAvailable(final SourceDataTag tag) {
        boolean result = tag.getCurrentValue() != null && tag.getCurrentValue().getValue() != null;
        if (equipmentLogger.isTraceEnabled())
            equipmentLogger.trace(format("isCurrentValueAvailable(%d) : %b", tag.getId(), result));
        return result;
    }

    /**
     * Checks if the tagValue is convertable to the value of the current tag.
     * 
     * @param tag The tag to check.
     * @param tagValue The value to check.
     * @return True if the value is convertable else false.
     */
    public boolean isConvertable(final SourceDataTag tag, final Object tagValue) {
        equipmentLogger.trace("entering isConvertable()..");
        String value = tagValue.toString();
        boolean isConvertable = TypeConverter.cast(value, tag.getDataType()) != null;
        equipmentLogger.trace("leaving isConvertable().. Result: " + isConvertable);
        return isConvertable;
    }

    /**
     * This method checks whether the equipment's time is too far in the future or not. For doing that, the equipment's
     * time is compared to the system's time
     * 
     * @param timestamp Time sent from the equipment in ms.
     * @return Whether the equipment's timestamp is inside the indicated time range or not
     */
    public boolean isTimestampValid(final long timestamp) {
        equipmentLogger.trace("entering isTimestampValid()..");
        boolean isValid = true;
        long diff = (timestamp - System.currentTimeMillis());
        if (diff > MAX_MSECONDS_DIFF) {
            isValid = false;
        }
        equipmentLogger.trace("leaving isTimestampValid().. Result: " + isValid);
        return isValid;
    }

    /**
     * This method is responsible for checking if new value received from data source fits in a proper range
     * 
     * @param sdt the source data tag object
     * @param value new value of SourceDataTag to be checked
     * @return True if the value is in range else false.
     */
    @SuppressWarnings("unchecked")
    public boolean isInRange(final SourceDataTag sdt, final Object value) {
        equipmentLogger.trace("entering isInRange()..");
        boolean result = true;
        if (sdt.getMinValue() != null) {
            if (sdt.getMinValue().compareTo(value) > 0) {
                equipmentLogger.trace("\tout of range : " + value + " is less than the authorized minimum value "
                        + sdt.getMinValue());
                result = false;
            }
        }
        if (result) {
            if (sdt.getMaxValue() != null) {
                if (sdt.getMaxValue().compareTo(value) < 0) {
                    equipmentLogger.trace("\tout of range : " + value
                            + " is greater than the authorized maximum value " + sdt.getMaxValue());
                    result = false;
                }
            }
        }
        equipmentLogger.trace("leaving isInRange(), value in range: " + result);
        return result;
    }

    
    /**
     * This method is responsible for checking if the new value of the particular SourceDataTag should be sent to the
     * application server or not. The decision is taken based on the deadband specification of the considered tag and
     * assumes that the new update is valid (= quality OK).
     * 
     * @param currentTag the current of the tag
     * @param newTagValue new value of the SourceDataTag, received from a data source.
     * @return True if the value is filtered else false.
     */
    public boolean isValueDeadbandFiltered(final SourceDataTag currentTag, final Object newTagValue,
        final String newTagValueDesc) {
      return isValueDeadbandFiltered(currentTag, newTagValue, newTagValueDesc, new SourceDataQuality());
    }
    
    /**
     * This method is responsible for checking if the new value of the particular SourceDataTag should be sent to the
     * application server or not. The decision is taken based on the deadband specification of the considered tag
     * 
     * @param currentTag the current of the tag
     * @param newTagValue new value of the SourceDataTag, received from a data source.
     * @param newTagValueDesc the new value description
     * @param newSDQuality the new tag quality
     * @return True if the value is filtered else false.
     */
      private boolean isValueDeadbandFiltered(final SourceDataTag currentTag, final Object newTagValue,
          final String newTagValueDesc, final SourceDataQuality newSDQuality) {
        if (equipmentLogger.isTraceEnabled()) {
            equipmentLogger.trace(format("entering valueDeadbandFilterOut(%d)..", currentTag.getId()));
        }

        boolean filterTag = false;
        float valueDeadband;
        if (currentTag.getAddress().isProcessValueDeadbandEnabled()) {

            if (isCurrentValueAvailable(currentTag) 
                && (currentTag.getCurrentValue().getQuality().getQualityCode() == newSDQuality.getQualityCode())) {
                valueDeadband = currentTag.getAddress().getValueDeadband();

                switch (currentTag.getDataTypeNumeric()) {
                // Same for all number types
                case TagDataType.TYPE_DOUBLE:
                case TagDataType.TYPE_FLOAT:
                case TagDataType.TYPE_INTEGER:
                case TagDataType.TYPE_LONG:
                    if (isCurrentValueAvailable(currentTag)) {
                        Number currentValue = (Number) currentTag.getCurrentValue().getValue();
                        Number newValue = newTagValue == null ? null : (Number) newTagValue;
                        // Switch between absolute and relative value deadband
                        switch (currentTag.getAddress().getValueDeadbandType()) {
                        case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE:
                            filterTag = isAbsoluteValueDeadband(currentValue, newValue, valueDeadband);
                            break;
                        case DataTagDeadband.DEADBAND_PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE:

                            String tagValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (tagValueDesc == null) {
                                tagValueDesc = "";
                            }

                            String newValueDesc = newTagValueDesc;
                            if (newValueDesc == null) {
                                newValueDesc = "";
                            }
                            String currentValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (currentValueDesc == null) {
                                currentValueDesc = "";
                            }

                            // check if the value description has changed, if yes - then do not apply deadband filtering
                            if (tagValueDesc.equals(newValueDesc)) {
                                filterTag = isAbsoluteValueDeadband(currentValue, newValue, valueDeadband);
                            }
                            break;

                        case DataTagDeadband.DEADBAND_PROCESS_RELATIVE:
                            filterTag = isRelativeValueDeadband(currentValue, newValue, valueDeadband);
                            break;
                        case DataTagDeadband.DEADBAND_PROCESS_RELATIVE_VALUE_DESCR_CHANGE:

                            tagValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (tagValueDesc == null) {
                                tagValueDesc = "";
                            }

                            newValueDesc = newTagValueDesc;
                            if (newValueDesc == null) {
                                newValueDesc = "";
                            }
                            currentValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (currentValueDesc == null) {
                                currentValueDesc = "";
                            }

                            // check if the value description has changed, if yes - then do not apply deadband filtering
                            if (tagValueDesc.equals(newValueDesc)) {
                                filterTag = isRelativeValueDeadband(currentValue, newValue, valueDeadband);
                            }
                            break;

                        default:
                            // do nothing
                            break;
                        }
                        break;
                    }
                default:
                    // It is currently not intended that non number values are
                    // range checked
                    break;
                }// switch

            }// if

        }// if

        if (equipmentLogger.isTraceEnabled()) {
            equipmentLogger.trace(format("leaving valueDeadbandFilterOut(%d); filter out = %b", currentTag.getId(), filterTag));
        }
      
        return filterTag;
    }

    /**
     * Returns true if the difference of the provided numbers is smaller than the value deadband.
     * 
     * @param currentValue The current value of the tag.
     * @param newValue The new value of the tag.
     * @param valueDeadband The value deadband.
     * @return True if the absolute value deadband fits else false.
     */
    public boolean isAbsoluteValueDeadband(final Number currentValue, final Number newValue, final float valueDeadband) {
        equipmentLogger.trace("entering isAbsoluteValueDeadband()..");
        boolean isAbsoluteValueDeadband = currentValue != null && newValue != null
                && Math.abs(currentValue.doubleValue() - newValue.doubleValue()) < valueDeadband;
        equipmentLogger.trace("leaving isAbsoluteValueDeadband().. Result: " + isAbsoluteValueDeadband);
        return isAbsoluteValueDeadband;
    }

    /**
     * Returns true if difference of the values is higher than the current value multiplied with the time deadband
     * (divided by 100).
     * 
     * @param currentValue The current value of the tag.
     * @param newValue The new value of the tag.
     * @param valueDeadband The value deadband in %.
     * @return True if the relative value deadband fits else false.
     */
    public boolean isRelativeValueDeadband(final Number currentValue, final Number newValue, final float valueDeadband) {
        equipmentLogger.trace("entering isRelativeValueDeadband()..");
        boolean isRelativeValueDeadband = false;
        if (currentValue == null || newValue == null) {
            isRelativeValueDeadband = false;
        } else if (currentValue.equals(newValue)) {
            isRelativeValueDeadband = true;
        } else {
            double curDoubleValue = currentValue.doubleValue();
            if (curDoubleValue != 0) {
                // valueDeadband divided by 100 to go from % to a factor
                double maxDiff = curDoubleValue * valueDeadband * PERCENTAGE_FACTOR;
                double realDiff = Math.abs(curDoubleValue - newValue.doubleValue());
                isRelativeValueDeadband = realDiff < maxDiff;
            }
        }
        equipmentLogger.trace("leaving isRelativeValueDeadband().. Result: " + isRelativeValueDeadband);
        return isRelativeValueDeadband;
    }
}
