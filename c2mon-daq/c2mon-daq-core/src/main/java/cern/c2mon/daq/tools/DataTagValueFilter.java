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
package cern.c2mon.daq.tools;

import static java.lang.String.format;

import java.util.Arrays;

import cern.c2mon.shared.common.datatag.*;
import cern.c2mon.shared.common.datatag.util.SourceDataTagQualityCode;
import cern.c2mon.shared.common.datatag.util.ValueDeadbandType;
import cern.c2mon.shared.common.filter.FilteredDataTagValue.FilterType;
import cern.c2mon.shared.common.type.TypeConverter;
import lombok.extern.slf4j.Slf4j;

/**
 * Class with all possible filters for Data Tag Values
 *
 * @author vilches
 */
@Slf4j
public class DataTagValueFilter {

    /**
     * Factor to calculate from a percentage value to a simple factor.
     */
    private static final double PERCENTAGE_FACTOR = 0.01;

    /**
     * Creates a new Data Tag Value Filter
     */
    public DataTagValueFilter() {
    }

    /**
     * This method is responsible for checking if the new value of the
     * particular SourceDataTag should be sent to the application server or not.
     * The decision is taken based on the deadband specification of the
     * considered tag and assumes that the new update is valid (= quality OK).
     *
     * @param currentTag
     *            the current of the tag
     * @param newTagValue
     *            new value of the SourceDataTag, received from a data source.
     * @return True if the value is filtered else false.
     */
    public boolean isValueDeadbandFiltered(final SourceDataTag currentTag, final ValueUpdate update) {
        return isValueDeadbandFiltered(currentTag, update, new SourceDataTagQuality());
    }

    /**
     * This method is responsible for checking if the new value of the
     * particular SourceDataTag should be sent to the application server or not.
     * The decision is taken based on the deadband specification of the
     * considered tag
     *
     * @return True if the value is filtered else false.
     */
    private boolean isValueDeadbandFiltered(final SourceDataTag currentTag, final ValueUpdate update,
            final SourceDataTagQuality newSDQuality) {
        if (log.isTraceEnabled()) {
            log.trace(format("entering valueDeadbandFilterOut(%d)..", currentTag.getId()));
        }

        boolean filterTag = false;
        float valueDeadband;
        if (currentTag.getAddress().isProcessValueDeadbandEnabled()) {

            if (isCurrentValueAvailable(currentTag)
                    && (currentTag.getCurrentValue().getQuality().getQualityCode() == newSDQuality.getQualityCode())) {
                valueDeadband = currentTag.getAddress().getValueDeadband();

                if (TypeConverter.isNumber(currentTag.getDataType())) {
                    if (isCurrentValueAvailable(currentTag)) {
                        Number currentValue = (Number) currentTag.getCurrentValue().getValue();
                        Number newValue = update.getValue() == null ? null : (Number) update.getValue();
                        
                        ValueDeadbandType valueDeadbandType = ValueDeadbandType.getValueDeadbandType((int) currentTag.getAddress().getValueDeadbandType());
                        // Switch between absolute and relative value deadband
                        switch (valueDeadbandType) {
                        case PROCESS_ABSOLUTE:
                            filterTag = isAbsoluteValueDeadband(currentValue, newValue, valueDeadband);
                            break;
                        case PROCESS_ABSOLUTE_VALUE_DESCR_CHANGE:

                            String tagValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (tagValueDesc == null) {
                                tagValueDesc = "";
                            }

                            String newValueDesc = update.getValueDescription();
                            if (newValueDesc == null) {
                                newValueDesc = "";
                            }
                            String currentValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (currentValueDesc == null) {
                                currentValueDesc = "";
                            }

                            // check if the value description has changed, if
                            // yes - then do
                            // not apply deadband filtering
                            if (tagValueDesc.equals(newValueDesc)) {
                                filterTag = isAbsoluteValueDeadband(currentValue, newValue, valueDeadband);
                            }
                            break;

                        case PROCESS_RELATIVE:
                            filterTag = isRelativeValueDeadband(currentValue, newValue, valueDeadband);
                            break;
                        case PROCESS_RELATIVE_VALUE_DESCR_CHANGE:

                            tagValueDesc = currentTag.getCurrentValue().getValueDescription();
                            if (tagValueDesc == null) {
                                tagValueDesc = "";
                            }

                            newValueDesc = update.getValueDescription();
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
                    }
                }

            } // if

        } // if

        if (log.isTraceEnabled()) {
            log.trace(format("leaving valueDeadbandFilterOut(%d); filter out = %b", currentTag.getId(), filterTag));
        }

        return filterTag;
    }

    /**
     * Compares the value, quality and time stamp information of the current
     * {@link SourceDataTagValue} against the newly received quality
     * information. Avoid sending twice (one by one) 2 invalid tags with the
     * same quality code and description
     *
     * Currently used by EquipmentSenderInvalid
     *
     * @return <code>FilterType</code>, if this the new quality is a candidate
     *         for being filtered out it will return the reason if not it will
     *         return <code>FilterType.NO_FILTERING</code>
     */
    public FilterType isCandidateForFiltering(final SourceDataTag currentTag, final ValueUpdate castedUpdate,
            final SourceDataTagQuality newSDQuality) {
        log.debug("isCandidateForFiltering - entering isCandidateForFiltering() for tag #" + currentTag.getId());

        SourceDataTagValue currentSDValue = currentTag.getCurrentValue();

        if (currentSDValue != null) {
            // Check if the new update is older or equal than the current value
            if (isOlderUpdate(newSDQuality, currentSDValue.getQuality(), castedUpdate.getSourceTimestamp(),
                    currentSDValue.getTimestamp().getTime())) {
                // Check if it is repeated value
                FilterType result = isRepeatedValue(currentTag, castedUpdate, newSDQuality);

                if ((result == FilterType.REPEATED_INVALID) || (result == FilterType.REPEATED_VALUE)) {
                    // The value will be filtered out by result (REPEATED_INVALID, REPEATED_VALUE)
                    return result;
                } else {
                    // The value will be filtered out by OLD_UPDATE
                    log.trace("Tag {} - New timestamp is older than the current timestamp. Candidate for filtering", currentSDValue.getId());
                    return FilterType.OLD_UPDATE;
                }
            }

            // Check if the value is
            return isRepeatedValue(currentTag, castedUpdate, newSDQuality);
        } else {
          // in case the SourceDataTag value has never been initialized we don't want to filter  
          log.trace("Tag {} - Current Source Data Tag Value null but we have a New value. Not candidate for filtering", currentTag.getId());
        }

        // We got a new quality information that we want to send to the server.
        return FilterType.NO_FILTERING;
    }

    /**
     * IN addition to isCandidateForFiltering it compares the value and quality
     * information of the current {@link SourceDataTagValue} against the newly
     * received quality information. Avoid sending twice (one by one) 2 invalid
     * tags with the same quality code and description
     *
     * @param currentTag
     *            The current tag object of the {@link SourceDataTag} that shall
     *            be updated
     * @param newValue
     *            The new update value that we want set to the tag
     * @param newTagValueDesc
     *            The new update value description
     * @param newSDQuality
     *            The new quality info for the {@link SourceDataTag} that shall
     *            be updated
     * @param newSourceTimestamp
     *            The new source timestamp
     *
     * @return <code>FilterType</code>, if this the new quality is a candidate
     *         for being filtered out it will return the reason if not it will
     *         return <code>FilterType.NO_FILTERING</code>
     */
    private FilterType isRepeatedValue(final SourceDataTag currentTag, final ValueUpdate update,
            final SourceDataTagQuality newSDQuality) {

        SourceDataTagValue currentSDValue = currentTag.getCurrentValue();
        FilterType filtering;

        if (update.getValue() != null && update.getValue().getClass().isArray()) {
            filtering = isDifferentArrayValue(currentTag, update.getValue());
        } else {
            filtering = isDifferentValue(currentTag, update, newSDQuality);
        }

        if (filtering != null) {
            return filtering;
        }

        // The two values are both null or equal. Now we check for redundant Value Description information
        if ((filtering = isDifferentValueDescription(currentSDValue, update.getValueDescription())) != null) {
            return filtering;
        }

        // Current and new Values and Value Descriptions are both null or equal. Now we check for redundant quality information
        if ((filtering = isDifferentDataTagQuality(currentSDValue, newSDQuality)) != null) {
            return filtering;
        }

        // We got a new quality information that we want to send to the server.
        return FilterType.NO_FILTERING;
    }

    private FilterType isDifferentValue(final SourceDataTag currentTag, final ValueUpdate update,
            final SourceDataTagQuality newSDQuality) {
        FilterType filtering = null;
        SourceDataTagValue currentSDValue = currentTag.getCurrentValue();

        if (currentSDValue.getValue() == null && update.getValue() != null) {
            // Got a new value which is initializing our SourceDataTag. Hence we do not want to filter it out!
            log.trace("Tag {} - Current Value null but we have a New value. Not candidate for filtering", currentSDValue.getId());
            return FilterType.NO_FILTERING;
        } else if (currentSDValue.getValue() != null && !currentSDValue.getValue().equals(update.getValue())) {
            // The two value are different, hence we do not want to filter it out ... unless the Value dead band filter said the opposite
            if (isValueDeadbandFiltered(currentTag, update, newSDQuality)) {
                log.trace("Tag {} - New value update but within value deadband filter. Candidate for filtering", currentSDValue.getId());
                return FilterType.VALUE_DEADBAND;
            }

            log.trace("Tag {} - Both Values are different (Current vs New) = ({} vs {}). Not candidate for filtering", currentSDValue.getId(), currentSDValue.getValue(), update.getValue());
            return FilterType.NO_FILTERING;
        }

        return filtering;
    }

    /**
     * Helper method which compares the values of the new DataTag and the vale
     * of the previous one. The type of the value is complex which means it is
     * either an array or an arbitrary object.
     *
     * @param currentTag
     * @param newValue
     * @param newTagValueDesc
     * @param newSDQuality
     *
     * @return
     */
    private FilterType isDifferentArrayValue(final SourceDataTag currentTag, final Object newValue) {
        FilterType filtering = null;
        SourceDataTagValue currentSDValue = currentTag.getCurrentValue();

        if (currentSDValue.getValue() == null && newValue != null) {
            // Got a new value which is initializing our SourceDataTag. Hence we do not want to filter it out!
            log.trace("Tag {} - Current Value null but we have a New value. Not candidate for filtering", currentSDValue.getId());
            return FilterType.NO_FILTERING;
        } else if (currentSDValue.getValue() != null && currentSDValue.getValue().getClass().isArray() && newValue.getClass().isArray()) {
            // The old value type and the new one are both arrays.
            if (!Arrays.equals((Object[]) currentSDValue.getValue(), (Object[]) newValue)) {
                log.trace("Tag {} - Both Values are different (Current vs New) = ({} vs {}). Not candidate for filtering", currentSDValue.getId(), currentSDValue.getValue(), newValue);
                return FilterType.NO_FILTERING;
            }
        // both values are no array so there must be an arbitrary object.
        } else if (currentSDValue.getValue() != null && !currentSDValue.getValue().equals(newValue)) {
            log.trace("isCandidateForFiltering - Tag {} - Both Values are different (Current vs New) = ({} vs {}). Not candidate for filtering", currentSDValue.getId(), currentSDValue.getValue(), newValue);
            return FilterType.NO_FILTERING;
        }

        return filtering;
    }

    private FilterType isDifferentValueDescription(SourceDataTagValue currentSDValue, final String newTagValueDesc) {
        FilterType filtering = null;

        // The two values are both null or equal. Now we check for redundant Value Description information
        if (!currentSDValue.getValueDescription().equalsIgnoreCase(newTagValueDesc)
                && ((newTagValueDesc != null) || !currentSDValue.getValueDescription().isEmpty())) {
            /*
             * Note 1: currentSDValue.getValueDescription() will never be null
             * Note 2: if getValueDescription is empty and newTagValueDesc is
             * null we get not equal but for us will be equal (no value) so we
             * take care of this special case and continue the checks
             */

            // The two value Descriptions are different
            log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                    + " - Both Values are equal but Value Descriptions are different. Not candidate for filtering");

            return FilterType.NO_FILTERING;
        }

        return filtering;
    }

    private FilterType isDifferentDataTagQuality(SourceDataTagValue currentSDValue,
            final SourceDataTagQuality newSDQuality) {
        FilterType filtering = null;

        if (currentSDValue.getQuality() != null) {
            // Check, if quality code did not change
            if ((currentSDValue.getQuality().getQualityCode() == newSDQuality.getQualityCode())) {
                // Only checks description is Quality is invalid
                if (!newSDQuality.isValid()) {
                    log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                            + " - Both Value, Value Description and Quality codes are equal. Check Quality Descriptions to take a decision");

                    // Check if quality description did not change. If it is not null we compare it with the new one
                    if (currentSDValue.getQuality().getDescription() == null) {
                        // If description is null we cannot compare so we check directly if both are null or not
                        if (newSDQuality.getDescription() == null) {
                            // We filter out since both are the same and null
                            log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                                    + " - Both Quality Descriptions are null. Candidate for filtering");

                            return FilterType.REPEATED_INVALID;
                        } else {
                            log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                                    + " - Current Quality Description null but we have a New Quality Description. Not candidate for filtering");
                            // Goes directly to the final return
                        }
                    }
                    // Description is not null. We can compare it with the new description
                    else if (currentSDValue.getQuality().getDescription().equals(newSDQuality.getDescription())) {
                        // If we are here, it means we have received a redundant quality code and description ==> should be filtered out.
                        log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                                + " - Both Value, Value Description, Quality and Quality Descriptions are equal. Candidate for filtering");

                        return FilterType.REPEATED_INVALID;
                    } else {
                        log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                                + " - Current Quality Description and New Quality Description are different. Not candidate for filtering");
                        // Goes directly to the final return
                    }
                } else {
                    log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                            + " - Both Value, Value Description and Quality codes (OK) are equal");

                    return FilterType.REPEATED_VALUE;
                }
            }
            // Different Quality Codes
            else {
                log.trace("isCandidateForFiltering - Tag " + currentSDValue.getId()
                        + " - Both Value and Value Description are equal but Quality Codes are different. Not candidate for filtering");
            }
        }
        return filtering;
    }

    /**
     * Returns true if the difference of the provided numbers is smaller than
     * the value deadband.
     *
     * @param currentValue
     *            The current value of the tag.
     * @param newValue
     *            The new value of the tag.
     * @param valueDeadband
     *            The value deadband.
     * @return True if the absolute value deadband fits else false.
     */
    public boolean isAbsoluteValueDeadband(final Number currentValue, final Number newValue,
            final float valueDeadband) {
        log.trace("entering isAbsoluteValueDeadband()..");
        Double delta = null;
        // No filtering if either value is null, as no comparison would be valid
        if (currentValue != null && newValue != null) {
            // Calculate a sensible delta (as long as incoming values wouldn't cause a loss of precision when subtracted).
            delta = calculateDelta(currentValue, newValue);
        }
        boolean isAbsoluteValueDeadband = delta != null && delta < valueDeadband;
        log.trace("leaving isAbsoluteValueDeadband().. Result: " + isAbsoluteValueDeadband);
        return isAbsoluteValueDeadband;
    }

    /**
     * We try to calculate the delta on a per-data type basis so as to reduce
     * the risk of precision loss when converting to double.
     * 
     * @param currentValue
     *            The current value.
     * @param newValue
     *            The new value.
     * @return <code>null</code> if no sensible delta could be calculated (e.g.
     *         loss of precision conversion), a Double with the delta otherwise.
     */
    private Double calculateDelta(final Number currentValue, final Number newValue) {
        // Note that Integer to Float, and Long to Double require particular attention, as they can incur a loss of precision
        if (willCausePrecisionLoss(currentValue, newValue)) {
            log.trace(
                    "Possible loss of precision detected on incoming values when evaluating against a float deadband");
            return null;

        }
        Double result = null;

        switch (currentValue.getClass().getName()) {
        case "java.lang.Integer":
            result = Math.abs((double) (currentValue.intValue() - newValue.intValue()));
            break;
        case "java.lang.Long":
            result = Math.abs((double) (currentValue.longValue() - newValue.longValue()));
            break;
        case "java.lang.Byte":
            result = Math.abs((double) ((byte) (currentValue.byteValue() - newValue.byteValue())));
            break;
        case "java.lang.Float":
            result = Math.abs((double) (currentValue.floatValue() - newValue.floatValue()));
            break;
        case "java.lang.Double":
            result = Math.abs(currentValue.doubleValue() - newValue.doubleValue());
            break;
        default:
            log.trace("Incoming numeric value of unknown type " + currentValue.getClass().getName());
        }

        return result;
    }

    /**
     * Check if the given numbers could cause a loss of precision when converted
     * to float or double.
     * 
     * @param values
     *            The given numbers.
     * @return <code>true</code> if any of the given integers could cause a loss
     *         of precision when converted to float.
     */
    static boolean willCausePrecisionLoss(final Number... values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof Integer) {
                Integer eval = (Integer) values[i];
                if (eval < 0) {
                    eval *= -1;
                }
                if (Integer.numberOfLeadingZeros(eval) + Integer.numberOfTrailingZeros(eval) < 8) {
                    return true;
                }
            }
            if (values[i] instanceof Long) {
                Long eval = (Long) values[i];
                if (eval < 0) {
                    eval *= -1;
                }
                if (Long.numberOfLeadingZeros(eval) + Long.numberOfTrailingZeros(eval) < 11) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if difference of the values is higher than the current value
     * multiplied with the time deadband (divided by 100).
     *
     * @param currentValue
     *            The current value of the tag.
     * @param newValue
     *            The new value of the tag.
     * @param valueDeadband
     *            The value deadband in %.
     * @return True if the relative value deadband fits else false.
     */
    public boolean isRelativeValueDeadband(final Number currentValue, final Number newValue,
            final float valueDeadband) {
        log.trace("entering isRelativeValueDeadband()..");
        boolean isRelativeValueDeadband = false;
        if (currentValue == null || newValue == null) {
            // do nothing
        } else if (currentValue.equals(newValue)) {
            isRelativeValueDeadband = true;
        } else {
            double curDoubleValue = currentValue.doubleValue();
            if (curDoubleValue != 0) {
                // valueDeadband divided by 100 to go from % to a factor
                double maxDiff = curDoubleValue * valueDeadband * PERCENTAGE_FACTOR;
                Double realDiff = calculateDelta(currentValue, newValue);
                isRelativeValueDeadband = realDiff != null && realDiff < maxDiff;
            }
        }
        log.trace("leaving isRelativeValueDeadband().. Result: " + isRelativeValueDeadband);
        return isRelativeValueDeadband;
    }

    /**
     * Checks if there is a not null value for this tag.
     *
     * @param tag
     *            The tag to check.
     * @return Returns true if a not null value is available else false.
     */
    private boolean isCurrentValueAvailable(final SourceDataTag tag) {
        boolean isAvailable = (tag.getCurrentValue() != null) && (tag.getCurrentValue().getValue() != null);

        if (log.isTraceEnabled())
            log.trace(format("isCurrentValueAvailable - Tag %d : %b", tag.getId(), isAvailable));

        return isAvailable;
    }

    /**
     * Checks if the new Timestamp is older than the current one and if so it
     * checks the Quality code to decide if the value has to be filtered out or
     * not.
     *
     * Filter when: - New TS < Current TS + Current has not DATA_UNAVAILABLE
     * Quality - New TS < Current TS + Current has DATA_UNAVAILABLE Quality +
     * New Bad Quality
     *
     * No filter when: - New TS < Current TS + Current has DATA_UNAVAILABLE
     * Quality + New Good Quality - New TS >= Current TS
     *
     * @param newSDQuality
     *            new Source Data Tag Quality
     * @param currentSDQuality
     *            current Source Data Tag Quality
     * @param newTimestamp
     *            new source Timestamp
     * @param currentTimestamp
     *            current source Timestamp
     * @return True if the New value has to be filter out. False if any other
     *         case.
     */
    protected boolean isOlderUpdate(final SourceDataTagQuality newSDQuality,
            final SourceDataTagQuality currentSDQuality, final long newTimestamp, final long currentTimestamp) {
        log.debug("isOlderUpdate - entering isOlderUpdate()");

        // if New TS is older to the current TS we may have a filtering use case
        if (newTimestamp < currentTimestamp) {
            log.trace("isOlderUpdate - New timestamp is older or equal than current TS (" + newTimestamp + ", "
                    + currentTimestamp + ")");
            // New timestamp is older or equal than current TS. Check the Quality
            if (currentSDQuality.getQualityCode() == SourceDataTagQualityCode.DATA_UNAVAILABLE) {
                // Exceptional case for not applying this filter:
                // If current tag was unavailable we allow sending tag value with good quality but old source time stamp
                if (newSDQuality.isValid()) {
                    // New value has Good Quality. Swapping to valid to invalid case. No filter
                    log.trace(
                            "isOlderUpdate - The current value has DATA_UNAVAILABLE Quality but new value has Good Quality. Not filter");
                    return false;
                } else {
                    // New value has Bad Quality. Filter
                    log.trace(
                            "isOlderUpdate - The current value has DATA_UNAVAILABLE Quality and new value has Bad Quality. Filter out ");
                    return true;
                }
            } else {
                // The current value has any Quality but DATA_UNAVAILABLE. Filter
                log.trace("isOlderUpdate - The current value quality is different to DATA_UNAVAILABLE. Filter out ");
                return true;
            }
        }

        // New TS is newer than current TS
        log.trace("isOlderUpdate - New timestamp is newer or equal than current TS. Not filter");
        return false;
    }
}
