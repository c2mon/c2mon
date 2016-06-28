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
package cern.c2mon.web.configviewer.statistics.daqlog.charts;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.web.configviewer.statistics.daqlog.C2MONChartStyles;
import cern.c2mon.web.configviewer.statistics.daqlog.DAQLogStatisticsMapper;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.GraphConfigException;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.InvalidTableNameException;

public abstract class WebChartCollection {

    /**
     * The collection of charts.
     */
    protected List<WebChart> webCharts;

    /**
     * The type of the charts in the collection.
     */
    protected Class chartClass;

    protected String chartIdPrefix;
    protected String paragraphTitle;
    protected String description;
    protected int imageXPixels;
    protected int imageYPixels;
    protected List<String> categoryList = new ArrayList<String>();

    /**
     * Reference to the {@link StatisticsMapper} bean.
     */
    DAQLogStatisticsMapper mapper;


    public abstract void configure(Element chartElement, C2MONChartStyles C2MONChartStyles)
            throws SQLException, GraphConfigException, InvalidTableNameException;

    /**
     *
     * @param chartElement
     * @return
     * @throws GraphConfigException
     * @throws SQLException
     * @throws InvalidTableNameException
     */

    public static final WebChartCollection fromXML(final Element chartElement, C2MONChartStyles C2MONChartStyles, final DAQLogStatisticsMapper mapper)
    throws GraphConfigException, SQLException, InvalidTableNameException {

        // get the chart implementation class and construct the chart
        String chartCollectionClass = chartElement.getAttribute("class");

        WebChartCollection webChartCollection;
        try {
            webChartCollection = (WebChartCollection) Class.forName(chartCollectionClass).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new GraphConfigException();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new GraphConfigException();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new GraphConfigException();
        }

        // initialise web chart collection list
        webChartCollection.webCharts = new ArrayList<WebChart>();

        // get the chart id prefix, used in the web file names
        webChartCollection.chartIdPrefix = chartElement.getAttribute("id-pref");

        // get the title of the html paragraph
        if (chartElement.getElementsByTagName("paragraph-title").item(0).getChildNodes().getLength() != 0) {
            webChartCollection.paragraphTitle = chartElement.getElementsByTagName("paragraph-title").item(0).getFirstChild().getNodeValue();
        } else {
            webChartCollection.paragraphTitle = "";
        }

        // get the chart description
        if (chartElement.getElementsByTagName("description").item(0).getChildNodes().getLength() != 0) {
            webChartCollection.description = chartElement.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
        } else {
            webChartCollection.description = "";
        }


        // get the size of the web image
        webChartCollection.imageXPixels = Integer.valueOf(chartElement.getElementsByTagName("x-pixels").item(0).getFirstChild().getNodeValue());
        webChartCollection.imageYPixels = Integer.valueOf(chartElement.getElementsByTagName("y-pixels").item(0).getFirstChild().getNodeValue());

        // get the categories this chart must be filed under
        NodeList categoryNodes = chartElement.getElementsByTagName("category");

        // one category is necessary
        if (categoryNodes.getLength() == 0) {
            throw new GraphConfigException();
        } else {
            for (int i = 0; i < categoryNodes.getLength(); i++) {
                //replace all .'s in category with / for directory structure
                webChartCollection.categoryList.add(categoryNodes.item(i).getFirstChild().getNodeValue().replaceAll("\\.", "/"));
            }
        }

        webChartCollection.setMapper(mapper);
        webChartCollection.configure(chartElement, C2MONChartStyles);
        return webChartCollection;

    }

    /**
     * Set the reference to the {@link StatisticsMapper} bean.
     *
     * @param mapper the mapper to set.
     */
    private void setMapper(DAQLogStatisticsMapper mapper) {
      this.mapper = mapper;
    }

    public void setChartClass(Class chartClass) {
        this.chartClass = chartClass;
    }

    public Class getChartClass() {
        return chartClass;
    }

    public List<WebChart> getWebCharts() {
        return webCharts;
    }


}
