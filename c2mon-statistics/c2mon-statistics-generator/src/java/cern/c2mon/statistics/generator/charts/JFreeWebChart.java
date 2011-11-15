package cern.c2mon.statistics.generator.charts;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageFormat;
import org.w3c.dom.Element;

import cern.c2mon.statistics.generator.TimChartStyles;
import cern.c2mon.statistics.generator.exceptions.GraphConfigException;
import cern.c2mon.statistics.generator.exceptions.InvalidTableNameException;
import cern.c2mon.statistics.generator.values.IChartValue;

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
    public abstract void configure(Element chartElement, TimChartStyles timChartStyles)
    throws GraphConfigException, SQLException, InvalidTableNameException;
    
    public abstract void configureMember(String memberName, WebChartCollection webChartCollection, Element chartElement, 
            TimChartStyles timChartStyles, List<IChartValue> list)
             throws GraphConfigException, SQLException, InvalidTableNameException;
    
    /**
     * Substitutes the member name in chart-specific attributes.
     * @param memberName
     */
    public abstract void chartSubMemberName(final String memberName);
    
    /**
     * Returns an image of the chart.
     * 
     * @return the image
     * @throws  
     */
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
    
    public final String getImageFormat() {
        return "png";
    }
    
    
    
    /**
     * Checks if the chart is configured and can be deployed.
     * @return true if can be deployed
     */
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
