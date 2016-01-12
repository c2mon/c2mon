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
package cern.c2mon.web.configviewer.statistics.daqlog.values;

/**
 * Class used for collecting the database values for collection StackedBarChart's (ibatis).
 * @author mbrightw
 *
 */
public class StackedBarChartCollectionValue extends StackedBarChartValue implements IChartCollectionValue {
    
    /**
     * The name of the chart this value is linked with.
     */
    private String memberName;
    
    /**
     * Implementation of the required interface method. Returns the underlying BarChartValue.
     * @return the value for the underlying chart
     */
    public final StackedBarChartValue returnChartValue() {
        return new StackedBarChartValue(getValue(), getSeriesKey(), getCategoryKey(), getGroup());
    }
    
    /**
     * @return the memberName
     */
    public final String getMemberName() {
        return memberName;
    }

    /**
     * @param memberName the memberName to set
     */
    public final void setMemberName(final String memberName) {
        this.memberName = memberName;
    }
}

