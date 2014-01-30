package cern.c2mon.daq.tools;

import static java.lang.String.format;
import cern.c2mon.daq.common.logger.EquipmentLogger;
import cern.c2mon.daq.common.logger.EquipmentLoggerFactory;
import cern.c2mon.shared.common.datatag.DataTagDeadband;
import cern.c2mon.shared.common.type.TagDataType;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;
import cern.c2mon.shared.daq.datatag.SourceDataTag;
import cern.c2mon.shared.daq.datatag.SourceDataTagValue;
import cern.c2mon.shared.daq.filter.FilteredDataTagValue.FilterType;

/**
 * Class with all possible filters for Data Tag Values
 * 
 * @author vilches
 */
public class DataTagValueFilter {
	/**
	 * The logger of this class
	 */
	protected EquipmentLogger equipmentLogger;
	
	 /**
     * Factor to calculate from a percentage value to a simple factor.
     */
    private static final double PERCENTAGE_FACTOR = 0.01;

	/**
	 * Creates a new Data Tag Value Filter which uses the provided equipment logger to log its results.
	 * 
	 * @param equipmentLogger The equipment logger to use
	 */
	public DataTagValueFilter(final EquipmentLoggerFactory equipmentLoggerFactory) {
		this.equipmentLogger = equipmentLoggerFactory.getEquipmentLogger(getClass());;
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
	 * 
	 * @return True if they have the same value else false.
	 */
	public boolean isSameValue(final SourceDataTag tag, final Object tagValue, final String valueDescr) {
		if (this.equipmentLogger.isTraceEnabled())
			this.equipmentLogger.trace(format("isSameValue - entering isSameValue(%d)..", tag.getId()));

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
			equipmentLogger.trace(format("isSameValue - leaving isSameValue(%d). Result: %b", tag.getId(), isSameValue));
		return isSameValue;
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
		if (this.equipmentLogger.isTraceEnabled()) {
			this.equipmentLogger.trace(format("entering valueDeadbandFilterOut(%d)..", currentTag.getId()));
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

		if (this.equipmentLogger.isTraceEnabled()) {
			this.equipmentLogger.trace(format("leaving valueDeadbandFilterOut(%d); filter out = %b", currentTag.getId(), filterTag));
		}

		return filterTag;
	}
	
	/**
     * Compares the value and quality information of the current {@link SourceDataTagValue} against the newly received 
     * quality information.
     * Avoid sending twice (one by one) 2 invalid tags with the same quality code and description
     * 
     * Currently used by EquipmentSenderInvalid
     * 
     * @param currentTag The current tag object of the {@link SourceDataTag} that shall be updated
     * @param newValue The new update value that we want set to the tag 
     * @param newTagValueDesc The new update value description
     * @param newSDQuality The new quality info for the {@link SourceDataTag} that shall be updated
     * 
     * @return <code>FilterType</code>, if this the new quality is a candidate for being filtered out it will return the 
     * reason if not it will return <code>FilterType.NO_FILTERING</code> 
     */
    public FilterType isCandidateForFiltering(final SourceDataTag currentTag, final Object newValue, final String newTagValueDesc,
        final SourceDataQuality newSDQuality) {
      short newQualityCode = newSDQuality.getQualityCode(); 
    
      SourceDataTagValue currentSDValue = currentTag.getCurrentValue();
      if (currentSDValue != null) {
        if (currentSDValue.getValue() == null && newValue != null) {
          // Got a new value which is initializing our SourceDataTag. Hence we do not want to filter it out!
          this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
              " - Current Value null but we have a New value. Not candidate for filtering");

          return FilterType.NO_FILTERING;
        }        
        else if (currentSDValue.getValue() != null && !currentSDValue.getValue().equals(newValue)) {  
          // The two value are different, hence we do not want to filter it out ... unless the Value dead band filter said the opposite
          if (isValueDeadbandFiltered(currentTag, newValue, newTagValueDesc, newSDQuality)) {
            this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                " - New value update but within value deadband filter. Candidate for filtering");
            
            return FilterType.VALUE_DEADBAND;
          }
          
          // The two values are different, so it is clear we do not want to filter it out!
          this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
              " - Both Values are different. Not candidate for filtering");
          
          return FilterType.NO_FILTERING;
        }
        // The two values are both null or equal. Now we check for redundant Value Description information
        else if (!currentSDValue.getValueDescription().equalsIgnoreCase(newTagValueDesc) 
            && ((newTagValueDesc != null) || !currentSDValue.getValueDescription().isEmpty())) {
          /* 
           * Note 1: currentSDValue.getValueDescription() will never be null
           * Note 2: if getValueDescription is empty and newTagValueDesc is null we get not equal but 
           *         for us will be equal (no value) so we take care of this special case and continue the checks
           */
          
          // The two value Descriptions are different
          this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
              " - Both Values are equal but Value Descriptions are different. Not candidate for filtering");

          return FilterType.NO_FILTERING;
        }
        // Current and new Values and Value Descriptions are both null or equal! Now we check for redundant quality information
        else if (currentSDValue.getQuality() != null) {
          // Check, if quality code did not change
          if ((currentSDValue.getQuality().getQualityCode() == newQualityCode)) {
            // Only checks description is Quality is no OK (Invalids)
            if(newQualityCode != SourceDataQuality.OK) {
              this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                  " - Both Value, Value Description and Quality codes are equal. Check Quality Descriptions to take a decision");
              
              // Check if quality description did not change. If it is not null we compare it with the new one
              if (currentSDValue.getQuality().getDescription() == null) {
                // If description is null we cannot compare so we check directly if both are null or not
                if (newSDQuality.getDescription() == null) {
                  // We filter out since both are the same and null
                  this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                      " - Both Quality Descriptions are null. Candidate for filtering");

                  return FilterType.REPEATED_INVALID;
                }
                else {
                  this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                      " - Current Quality Description null but we have a New Quality Description. Not candidate for filtering");

                  // Goes directly to the final return
                }
              } 
              // Description is not null. We can compare it with the new description
              else if (currentSDValue.getQuality().getDescription().equals(newSDQuality.getDescription())) {
                // If we are here, it means we have received a redundant quality code and description ==> should be filtered out.
                this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                    " - Both Value, Value Description, Quality and Quality Descriptions are equal. Candidate for filtering");

                return FilterType.REPEATED_INVALID;
              }
            } else {
              this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                  " - Both Value, Value Description and Quality codes (OK) are equal");
              
              return FilterType.REPEATED_VALUE;
            }
          }
          // Different Quality Codes 
          else {
            this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentSDValue.getId() + 
                " - Both Value and Value Description are equal but Quality Codes are different. Not candidate for filtering");
          }
        }
      } 
      // in case the SourceDataTag value has never been initialized we don't want to filter
      else {
        this.equipmentLogger.trace("isCandidateForFiltering - Tag " + currentTag.getId() + 
            " - Current Source Data Tag Value null but we have a New value. Not candidate for filtering");
      }
      
      // We got a new quality information that we want to send to the server.
      return FilterType.NO_FILTERING;
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
        this.equipmentLogger.trace("entering isAbsoluteValueDeadband()..");
        boolean isAbsoluteValueDeadband = currentValue != null && newValue != null
                && Math.abs(currentValue.doubleValue() - newValue.doubleValue()) < valueDeadband;
        this.equipmentLogger.trace("leaving isAbsoluteValueDeadband().. Result: " + isAbsoluteValueDeadband);
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
        this.equipmentLogger.trace("entering isRelativeValueDeadband()..");
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
        this.equipmentLogger.trace("leaving isRelativeValueDeadband().. Result: " + isRelativeValueDeadband);
        return isRelativeValueDeadband;
    }

	/**
	 * Checks if there is a not null value for this tag.
	 * 
	 * @param tag The tag to check.
	 * @return Returns true if a not null value is available else false.
	 */
	private boolean isCurrentValueAvailable(final SourceDataTag tag) {
		boolean isAvailable = (tag.getCurrentValue() != null) && (tag.getCurrentValue().getValue() != null);

		if (this.equipmentLogger.isTraceEnabled())
			this.equipmentLogger.trace(format("isCurrentValueAvailable - Tag %d : %b", tag.getId(), isAvailable));

		return isAvailable;
	}
}
