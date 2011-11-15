package cern.c2mon.statistics.generator.styles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.PieDataset;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class stores the generic style used for all pie charts of
 * the statistics display module.
 * 
 * @author mbrightw
 *
 */
public class PieChartStyle {
     
    /**
     * An array of colours to be used for the pie chart.
     */
    private ArrayList<Color> colours;
    
    
    /**
     * Private constructor overriding the public one.
     */
    private PieChartStyle() {
        colours = new ArrayList<Color>();
    } 

    
    /**
     * Constructs a PieChartStyle object from the XML style element.
     * 
     * @param styleElement the XML element containing all the style info
     * @return the PieChartStyle generated from the XML
     */
    public static PieChartStyle fromXML(final Element styleElement) {
        //create new  PieChartStyle object
        PieChartStyle pieChartStyle = new PieChartStyle();
        
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
            pieChartStyle.colours.add(new Color(rValue, gValue, bValue));           
        } 
        return pieChartStyle;
    }
    
    
    /**
     * Applied the chart style to the chart passed as parameter.
     * 
     * @param chart the chart to apply the style to
     * @param dataset the dataset associated with the chart
     */
    public void applyTo(JFreeChart chart, final PieDataset dataset) {
        PiePlot plot = (PiePlot) chart.getPlot();
        List<String> keys = dataset.getKeys();
        int coloursSize = colours.size();
        int colourMod;
        for (int i = 0; i < keys.size(); i++) {
            colourMod = i % coloursSize;
            plot.setSectionPaint(keys.get(i), this.colours.get(colourMod));
        }

    }



   
}
