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
 * This class represents a value to be added to a bar chart.
 * 
 * @author mbrightw
 *
 */
public class BarChartValue implements IChartValue {
    
    /**
     * The value of the statistic.
     */
    private double value;
    
    /**
     * The series key.
     */
    private String seriesKey;

    /**
     * The category key.
     */
    private String categoryKey;

    /**
     * Default constructor (used by extensions).
     */
    public BarChartValue() {
        
    }
    
    /**
     * Constructor.
     * 
     * @param pValue
     * @param pSeriesKey
     * @param pCategoryKey
     */
    public BarChartValue(double pValue, String pSeriesKey, String pCategoryKey) {
        setCategoryKey(pCategoryKey);
        setSeriesKey(pSeriesKey);
        setValue(pValue);
    }
    
    /**
     * @return the value
     */
    public final double getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public final void setValue(final double value) {
        this.value = value;
    }

    /**
     * @return the seriesKey
     */
    public final String getSeriesKey() {
        return seriesKey;
    }

    /**
     * @param seriesKey the seriesKey to set
     */
    public final void setSeriesKey(final String seriesKey) {
        this.seriesKey = seriesKey;
    }

    /**
     * @return the categoryKey
     */
    public final String getCategoryKey() {
        return categoryKey;
    }

    /**
     * @param categoryKey the categoryKey to set
     */
    public final void setCategoryKey(final String categoryKey) {
        this.categoryKey = categoryKey;
    }
    
    
}
