/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.shared.daq.filter;

import org.apache.log4j.or.ObjectRenderer;

/**
 * The renderer class of the FilteredDataTagValue class, used by
 * log4j.
 * 
 * @author mbrightw
 *
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
