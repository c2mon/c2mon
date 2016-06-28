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
 * Represents a single value in a stacked bar chart.
 * 
 * @author mbrightw
 *
 */
public class StackedBarChartValue extends BarChartValue implements IChartValue {
    
    /**
     * The stacked bar group that the value belongs to.
     */
    private String group;

    /**
     * Default public constructor for subclasses.
     */
    public StackedBarChartValue() {
    }
    
    /**
     * Constructor.
     * 
     * @param pValue
     * @param pSeriesKey
     * @param pCategoryKey
     * @param group
     */
    public StackedBarChartValue(double pValue, String pSeriesKey, String pCategoryKey, String group) {
        setCategoryKey(pCategoryKey);
        setSeriesKey(pSeriesKey);
        setValue(pValue);
        setGroup(group);
    }
    
    /**
     * @return the group
     */
    public final String getGroup() {
        return group;
    }

    /**
     * @param group the group to set
     */
    public final void setGroup(final String group) {
        this.group = group;
    }
    
    

}
