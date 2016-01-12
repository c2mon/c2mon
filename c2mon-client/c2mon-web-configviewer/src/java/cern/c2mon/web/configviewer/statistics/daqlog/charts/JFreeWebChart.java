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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.w3c.dom.Element;

import cern.c2mon.web.configviewer.statistics.daqlog.C2MONChartStyles;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.GraphConfigException;
import cern.c2mon.web.configviewer.statistics.daqlog.exceptions.InvalidTableNameException;
import cern.c2mon.web.configviewer.statistics.daqlog.values.IChartValue;

/**
 * This class represents a chart (JFreeChart) that will be written to the web:
 * this consists of a chart together with related display information for the
 * html page, such as a graph description, paragraph title and size of the image.
 *
 * @author mbrightw
 *
 */
public abstract class JFreeWebChart extends WebChart {

    /**
     * The wrapped JFreeChart.
     */
    JFreeChart jFreeChart;

    /**
     * Must be provided in implementation of abstract class.
     * Sets the jFreeChart field.
     */
    @Override
    public abstract void configure(Element chartElement, C2MONChartStyles C2MONChartStyles)
    throws GraphConfigException, SQLException, InvalidTableNameException;

    public abstract void configureMember(String memberName, WebChartCollection webChartCollection, Element chartElement,
            C2MONChartStyles C2MONChartStyles, List<IChartValue> list)
             throws GraphConfigException, SQLException, InvalidTableNameException;

    /**
     * Substitutes the member name in chart-specific attributes.
     * @param memberName
     */
    @Override
    public abstract void chartSubMemberName(final String memberName);

    /**
     * Returns an image of the chart.
     *
     * @return the image
     * @throws
     */
    @Override
    public byte[] returnImage() {
        BufferedImage bufferedImage = jFreeChart.createBufferedImage(imageXPixels, imageYPixels);
        byte[] image = null;
        try {
            image = EncoderUtil.encode(bufferedImage, ImageFormat.PNG);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return image;
    }

    @Override
    public final String getImageFormat() {
        return "png";
    }



    /**
     * Checks if the chart is configured and can be deployed.
     * @return true if can be deployed
     */
    @Override
    public final boolean canDeploy() {
        return jFreeChart != null;
    }

    /**
     * Getter method for the JFreeChart.
     *
     * @return the jFreeChart
     */
    public final JFreeChart getJFreeChart() {
        return jFreeChart;
    }

    protected void setJFreeChart(JFreeChart freeChart) {
        jFreeChart = freeChart;
    }



}
