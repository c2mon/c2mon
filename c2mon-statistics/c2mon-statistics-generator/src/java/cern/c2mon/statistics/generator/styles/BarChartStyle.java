package cern.c2mon.statistics.generator.styles;

import java.awt.Color;
import java.awt.GradientPaint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.KeyToGroupMap;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class stores the generic style used for all bar charts of
 * the statistics display module.
 * 
 * @author mbrightw
 *
 */
public class BarChartStyle {
     
    /**
     * An array of colours to be used for the bar chart.
     */
    private ArrayList<Color> colours;
    
    
    /**
     * Private constructor overriding the public one.
     */
    private BarChartStyle() {
        colours = new ArrayList<Color>();
    } 

    
    /**
     * Constructs a BarChartStyle object from the XML style element.
     * 
     * @param styleElement the XML element containing all the style info
     * @return the PieChartStyle generated from the XML
     */
    public static BarChartStyle fromXML(final Element styleElement) {
        //create new  PieChartStyle object
        BarChartStyle barChartStyle = new BarChartStyle();
        
        // get the colours
        Element colourElement = (Element) styleElement.getElementsByTagName("colours").item(0);
        
        //get a list of colour nodes
        NodeList colourNodes = colourElement.getElementsByTagName("rgb");
        int listLength = colourNodes.getLength();
        Element currentColour;
        //iterate through all the colour nodes and set store them in the style object
        for (int nodeIndex = 0; nodeIndex < listLength; nodeIndex++) {
            currentColour = (Element) colourNodes.item(nodeIndex);
            int rValue = Integer.valueOf(currentColour.getElementsByTagName("r").item(0).getFirstChild().getNodeValue());
            int gValue = Integer.valueOf(currentColour.getElementsByTagName("g").item(0).getFirstChild().getNodeValue());
            int bValue = Integer.valueOf(currentColour.getElementsByTagName("b").item(0).getFirstChild().getNodeValue());
            //add new colour to the object been constructed
            barChartStyle.colours.add(new Color(rValue, gValue, bValue));           
        } 
        return barChartStyle;
    }
    
    
    /**
     * Apply the chart style to the chart passed as parameter.
     * 
     * @param chart the chart to apply the style to (is modified by the method)
     * @param dataset the dataset associated with the chart
     */
    public void applyTo(JFreeChart chart, final DefaultCategoryDataset dataset) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        int seriesCount = dataset.getRowCount();
        int coloursSize = colours.size();      
        int colourMod;
        for (int series = 0; series < seriesCount; series++) {
            colourMod = series % coloursSize;
            renderer.setSeriesPaint(series, this.colours.get(colourMod));
        }

    }
    
    /**
     * For stacked bar charts, one must additionally pass a Hash map specifying which series are in 
     * which groups.
     * 
     * @param chart the chart to apply the style to (is modified by the method)
     * @param dataset the dataset associated with the chart
     * @param groupMapping the mapping associating series to groups
     */
    public void applyTo(JFreeChart chart, final DefaultCategoryDataset dataset, final HashMap groupMapping) {
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        GroupedStackedBarRenderer renderer = new GroupedStackedBarRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setDrawBarOutline(false);
        renderer.setShadowVisible(false);
        
        int seriesCount = dataset.getRowCount();
        int coloursSize = colours.size();      
        int colourMod;
        for (int series = 0; series < seriesCount; series++) {
            colourMod = series % coloursSize;
            renderer.setSeriesPaint(series, this.colours.get(colourMod));
        }
        
        //get one of groups to set as default
        KeyToGroupMap map;
        Iterator initializeIterator = groupMapping.keySet().iterator();
        if (initializeIterator.hasNext()) {
            map = new KeyToGroupMap((String) groupMapping.get(initializeIterator.next()));
            
        } else {
            map = new KeyToGroupMap("no data found");
        }
        
        //map the key (series) to the correct group
        Iterator it = groupMapping.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
                map.mapKeyToGroup((String) key, (String) groupMapping.get(key));
            }
            renderer.setSeriesToGroupMap(map);
            plot.setRenderer(renderer);
    }



   
}
