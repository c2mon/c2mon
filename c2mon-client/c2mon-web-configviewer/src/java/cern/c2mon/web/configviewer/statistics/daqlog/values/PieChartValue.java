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
 * Represents a value (or segment) in a pie chart.
 * 
 * @author mbrightw
 *
 */
public class PieChartValue implements IChartValue {
    
    /**
     * The value of the pie segment to represent.
     */
    private double value;
    
    /**
     * The key of the pie segment.
     */
    private String key;

    /**
     * Default constructor.
     */
    public PieChartValue() {
        
    }
    /**
     * Constructor.
     * 
     * @param value the value of the statistic
     * @param key the pie chart key
     */
    public PieChartValue(double value, String key) {
        setValue(value);       
        this.key = key;
    }

    /**
     * Getter method for value attribute.
     * 
     * @return the value
     */
    public final double getValue() {
        return value;
    }

    /**
     * Setter method for value attribute.
     * 
     * @param value the value to set
     */
    public final void setValue(final double value) {
        this.value = value;
    }

    /**
     * Getter method for key attribute.
     * 
     * @return the key
     */
    public final String getKey() {
        return key;
    }

    /**
     * Setter method for key attribute.
     * 
     * @param key the key to set
     */
    public final void setKey(final String key) {
        this.key = key;
    }
}
