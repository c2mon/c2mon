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
package cern.c2mon.statistics.generator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cern.c2mon.statistics.generator.styles.BarChartStyle;
import cern.c2mon.statistics.generator.styles.PieChartStyle;

/**
 * Collects together all the chart styles for the
 * TIM website.
 * 
 * @author mbrightw
 *
 */
public class TimChartStyles {
    
    /**
     * Contains the generic style that will be applied to all pie diagrams
     * on the webpage.
     * 
     * Is configured from the graph XML config document (pie-chart-style element)
     */
    private PieChartStyle pieChartStyle;
    
    /**
     * Contains the generic style that will be applied to all pie diagrams
     * on the webpage.
     * 
     * Is configured from the graph XML config document (pie-chart-style element)
     */
    private BarChartStyle barChartStyle;
    
    /**
     * Override public default constructor.
     */
    private TimChartStyles() {
        pieChartStyle = null;
        barChartStyle = null;
    }
    
    public static TimChartStyles fromXML(Document graphXMLDocument) {
        TimChartStyles timChartStyles = new TimChartStyles();
        
        //get the pie chart style information
        Element pieStyleElement = (Element) graphXMLDocument.getElementsByTagName("pie-chart-style").item(0);
        timChartStyles.pieChartStyle = PieChartStyle.fromXML(pieStyleElement);
        
        //get the bar chart style information
        Element barStyleElement = (Element) graphXMLDocument.getElementsByTagName("bar-chart-style").item(0);
        timChartStyles.barChartStyle = BarChartStyle.fromXML(barStyleElement);
        
        return timChartStyles;
    }

    /**
     * Getter for the pie chart style.
     * 
     * @return the pieChartStyle
     */
    public PieChartStyle getPieChartStyle() {
        return pieChartStyle;
    }

    /**
     * Getter for the bar chart style.
     * 
     * @return the barChartStyle
     */
    public BarChartStyle getBarChartStyle() {
        return barChartStyle;
    }
    


}
