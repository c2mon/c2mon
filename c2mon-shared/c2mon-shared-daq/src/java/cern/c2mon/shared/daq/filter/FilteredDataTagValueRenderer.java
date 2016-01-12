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

package cern.c2mon.shared.daq.filter;

import org.apache.log4j.or.ObjectRenderer;

import cern.c2mon.shared.common.filter.FilteredDataTagValue;

/**
 * The renderer class of the FilteredDataTagValue class, used by
 * log4j.
 *
 * @author mbrightw
 * @deprecated since we switched to slf4j, implementation-specific features like this are not a good idea
 */
public class FilteredDataTagValueRenderer implements ObjectRenderer {

    /**
     * The default constructor.
     */
    public FilteredDataTagValueRenderer() {

    }

    /**
     * Returns the String representation of the object used in the log file.
     *
     * @param o the object to render
     * @return the string that appears in the file
     */
    public final String doRender(final Object o) {
        if (o != null) {
            // is correct object type
            if (o instanceof FilteredDataTagValue) {
                FilteredDataTagValue tag = (FilteredDataTagValue) o;
                StringBuffer str = new StringBuffer();

                str.append(tag.getId());
                str.append('\t');
                str.append(tag.getName());
                str.append('\t');
                str.append(tag.getTimestamp());
                str.append('\t');
                str.append(tag.getDataType());
                str.append('\t');
                str.append(tag.getValue());
                str.append('\t');

                str.append(tag.isDynamicFiltered());
                str.append('\t');
                str.append(tag.getFilterApplied());
                if (tag.getQualityCode() != null || tag.getQualityCode() != 0) {
                    str.append('\t');
                    str.append(tag.getQualityCode());

                } else {
                    str.append("\tOK");
                }
                if (tag.getQualityDescription() != null) {
                    str.append('\t');
                    str.append(tag.getQualityDescription());
                }
                if (tag.getValueDescription() != null) {
                    str.append('\t');
                    str.append(tag.getValueDescription());
                }
                return str.toString();
            }
            // wrong object type passed
            else {
                return o.toString();
            }
        }
        // null value passed
        else {
            return null;
        }
    }
}
