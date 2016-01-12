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
package cern.c2mon.statistics.generator.charts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import cern.c2mon.statistics.generator.TimChartStyles;
import cern.c2mon.statistics.generator.exceptions.GraphConfigException;
import cern.c2mon.statistics.generator.exceptions.InvalidTableNameException;
import cern.c2mon.statistics.generator.values.IChartCollectionValue;
import cern.c2mon.statistics.generator.values.IChartValue;

public abstract class JFreeWebChartCollection extends WebChartCollection {
    
    protected String title;
    
    protected List<IChartCollectionValue> chartValues;
    
    public abstract List<IChartCollectionValue> getValuesFromDatabase(String tablename) throws SQLException;
    
    public void configure(Element chartElement, TimChartStyles timChartStyles) 
            throws SQLException, GraphConfigException, InvalidTableNameException {
        //get the data
        String tablename = chartElement.getElementsByTagName("database-table").item(0).getFirstChild().getNodeValue();
        chartValues = getValuesFromDatabase(tablename);
        
        //retrieve the values for each individual chart
        HashMap<String, List<IChartValue>> collectionHash = new HashMap();
        Iterator<IChartCollectionValue> it = chartValues.iterator();
        while (it.hasNext()) {
            IChartCollectionValue currentCollectionValue = (IChartCollectionValue) it.next();
            //if the hash map does not have this chart already, create the key
            if (!collectionHash.containsKey(currentCollectionValue.getMemberName())) {
                collectionHash.put(currentCollectionValue.getMemberName(), new ArrayList<IChartValue>());
                //set the 
            }
            collectionHash.get(currentCollectionValue.getMemberName()).add(currentCollectionValue.returnChartValue());
        
    
        }
        
        //run through all the member name and construct the individual charts
        Iterator memberIt = collectionHash.keySet().iterator();
        while (memberIt.hasNext()) {
            JFreeWebChart jFreeWebChart;
            try {
                jFreeWebChart = (JFreeWebChart) getChartClass().newInstance();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                throw new RuntimeException();
            }
            Object memberName = memberIt.next();
            jFreeWebChart.configureMember((String) memberName, (WebChartCollection) this, chartElement, timChartStyles, collectionHash.get(memberName));
            webCharts.add(jFreeWebChart);
        }
        
    }

    
    
}

