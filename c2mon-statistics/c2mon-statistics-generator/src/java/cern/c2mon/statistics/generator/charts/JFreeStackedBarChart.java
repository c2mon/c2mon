package cern.c2mon.statistics.generator.charts;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.Element;

import cern.c2mon.statistics.generator.SqlMapper;
import cern.c2mon.statistics.generator.TimChartStyles;
import cern.c2mon.statistics.generator.exceptions.GraphConfigException;
import cern.c2mon.statistics.generator.exceptions.InvalidTableNameException;
import cern.c2mon.statistics.generator.styles.BarChartStyle;
import cern.c2mon.statistics.generator.values.IChartValue;
import cern.c2mon.statistics.generator.values.StackedBarChartValue;

public class JFreeStackedBarChart extends JFreeBarChart {
    
    
   
    public void configure(Element chartElement, TimChartStyles timChartStyles) throws SQLException, GraphConfigException, InvalidTableNameException {
        if (logger.isDebugEnabled()) {
            logger.debug("retrieving stacked bar chart data from database");
        }
        
        getParametersFromXML(chartElement);
        
        //get the dataset and the group mapping for the stacked bar chart
        HashMap datasetHash = getStackedBarChartData(tableName);    
    
        //create the stacked bar chart
        jFreeChart = createChart((Object) datasetHash, timChartStyles.getBarChartStyle());
    }  
    
    /**
     * Configures the JFreeBarChart when the chart is the member of a collection.
     * @throws InvalidTableNameException 
     */
//    public void configureMember(String memberName, Element chartElement, TimChartStyles timChartStyles, 
//            List<IChartValue> valueList) throws GraphConfigException, SQLException, InvalidTableNameException {
//            setGlobalParameters(memberName, webChartCollection);         
//            //get parameters from XML file and substitute member name
//            getParametersFromXML(memberName, chartElement);
//            jFreeChart = createChart(toDataset(valueList), timChartStyles.getBarChartStyle());
//            
//    }
//    
    
    private HashMap getStackedBarChartData(final String tableName) throws SQLException {
        //retrieve the chart values from the database
        List chartValues = SqlMapper.getStackedBarChartData(tableName);        
        return (HashMap) toDataset(chartValues);
        
    }
    
    protected Object toDataset(List<IChartValue> valueList) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Iterator<IChartValue> it = valueList.iterator();
        StackedBarChartValue currentValue;
        HashMap<String, String> groupMapping = new HashMap<String, String>();
        //add the values to the dataset one by one, and to the groupMapping
        while (it.hasNext()) {
            currentValue = (StackedBarChartValue) it.next();
            dataset.addValue(currentValue.getValue(), currentValue.getSeriesKey(), currentValue.getCategoryKey());
            if (!groupMapping.containsKey(currentValue.getSeriesKey())) {
                groupMapping.put(currentValue.getSeriesKey(), currentValue.getGroup());
            }   
        }
        HashMap datasetHashmap = new HashMap();
        datasetHashmap.put("dataset", dataset);
        datasetHashmap.put("groupMapping", groupMapping);
        return (Object) datasetHashmap;     
    }
    
  
    protected JFreeChart createChart(Object datasetHashObject, BarChartStyle barChartStyle) throws SQLException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering createChart()...");
        }
        HashMap datasetHash = (HashMap) datasetHashObject;
        
        //create the bar chart
        JFreeChart jFreeChart = ChartFactory.createStackedBarChart(
                title,                                                  // chart title
                domainAxis,                                             // domain axis label
                rangeAxis,                                              // range axis label
                (DefaultCategoryDataset) datasetHash.get("dataset"),    // data
                plotOrientation,                                        // orientation
                true,                                                   // include legend
                true,                                                   // tooltips?
                false                                                   // URLs?
            );
        
        //apply the TIM bar chart style to the chart
        barChartStyle.applyTo(jFreeChart, (DefaultCategoryDataset) datasetHash.get("dataset"), (HashMap) datasetHash.get("groupMapping"));
        if (logger.isDebugEnabled()) {
            logger.debug("...leaving createChart()");
        }
        return jFreeChart;
    }
}
