package cern.c2mon.statistics.generator.charts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.statistics.generator.TimChartStyles;
import cern.c2mon.statistics.generator.exceptions.GraphConfigException;
import cern.c2mon.statistics.generator.exceptions.InvalidTableNameException;

/**
 * The abstract class that every web chart implementation must extend.
 * @author mbrightw
 *
 */
public abstract class WebChart {
    
    /**
     * The prefix of the file name where the chart images are stored
     * (the full file name is CHART_FILE_NAME followed by the chart id and .png).
     */
    public static final String CHART_FILE_NAME = "chart_";
    
    /**
     * The prefix of all the html fragments.
     */
    public static final String HTML_FRAG_NAME = "statistics_include_";
    
    /**
     * The logger;
     */
    protected static Logger logger = Logger.getLogger(WebChart.class);
    
    /**
     * The id of the web chart.
     */
    String chartId;
    
    /**
     * The paragraph of the HTML section.
     */
    String paragraphTitle;
    
    /**
     * The description of the graph.
     */
    String graphDescription;
    
    /**
     * The size of the image in pixels (x axis).
     */
    int imageXPixels;
    
    /**
     * The size of the image in pixels (y axis).
     */
    int imageYPixels;
    
    /**
     * The categories in which this chart belongs
     * (format in XML is "cat1.cat2").
     */
    List<String> categories;
    
    /**
     * Does the specific configuration for the chart at hand.
     * 
     * @param chartElement the chart XML fragment
     * @param reference to the collection of chart styles used in the application
     * @throws GraphConfigException problem recognizing graph configuration parameters
     * @throws SQLException problem in getting data from database
     * @throws InvalidTableNameException the table name does not satisfy the required [a-zA-Z0-9_]+ format
     */
    public abstract void configure(final Element chartElement, final TimChartStyles timChartStyles)
    throws GraphConfigException, SQLException, InvalidTableNameException;
    
    /**
     * Substitutes the member name in chart-specific attributes.
     * @param memberName
     */
    public abstract void chartSubMemberName(final String memberName);
    
    /**
     * Checks if the wrapped chart is ready for deployment.
     * @return true is ready for deployment
     */
    public abstract boolean canDeploy();
    
    
    /**
     * Returns an image file. 
     * @return the image file of the chart
     */
    public abstract byte[] returnImage();
    
    /**
     * Returns the format of the image returned by returnImage.
     * @return the image format file ending
     */
    public abstract String getImageFormat();
        
    /**
     * Method to construct WebChart objects from the XML description
     * in the configuration file. This method deals with the common configuration
     * parameters for all WebCharts. The specific chart configuration is done by the
     * configure method in the implementation class.
     * 
     * @param chartElement the XML chart element
     * @param timChartStyles a reference to the collection of standard styles for TIM charts
     * @return the WebChart (may be null if not recognised)
     * @throws SQLException if problem with retrieving data from database
     * @throws GraphConfigException if the graph configuration file cannot be parsed or has incorrect entrie
     * @throws InvalidTableNameException if the table (view) is not of the allowed format (alphanumeric + _)
     */
    public static WebChart fromXML(final Element chartElement, final TimChartStyles timChartStyles) 
    throws SQLException, GraphConfigException, InvalidTableNameException {
    
        //JFreeChart chart = null;
        
        // get the chart implementation class and construct the chart
        String chartClass = chartElement.getAttribute("class");
        
        WebChart webChart;
        try {
            webChart = (WebChart) Class.forName(chartClass).newInstance();
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
             
        // get the chart id, used in the web files
        webChart.chartId = chartElement.getAttribute("id");
                
        // get the title of the html paragraph       
        if (chartElement.getElementsByTagName("paragraph-title").item(0).getChildNodes().getLength() != 0) {
            webChart.paragraphTitle = chartElement.getElementsByTagName("paragraph-title").item(0).getFirstChild().getNodeValue();
        } else {
            webChart.paragraphTitle = "";
        }  
        
        // get the chart description       
        if (chartElement.getElementsByTagName("description").item(0).getChildNodes().getLength() != 0) {
            webChart.graphDescription = chartElement.getElementsByTagName("description").item(0).getFirstChild().getNodeValue();
        } else {
            webChart.graphDescription = "";
        }
        
        // get the size of the web image 
        webChart.imageXPixels = Integer.valueOf(chartElement.getElementsByTagName("x-pixels").item(0).getFirstChild().getNodeValue());
        webChart.imageYPixels = Integer.valueOf(chartElement.getElementsByTagName("y-pixels").item(0).getFirstChild().getNodeValue());
        
        // get the categories this chart must be filed under
        NodeList categoryNodes = chartElement.getElementsByTagName("category");
        List<String> categoryList = new ArrayList<String>(); 
    
        // one category is necessary
        if (categoryNodes.getLength() == 0) {
            throw new GraphConfigException();
        } else {          
            for (int i = 0; i < categoryNodes.getLength(); i++) {
                //replace all .'s in category with / for directory structure
                categoryList.add(categoryNodes.item(i).getFirstChild().getNodeValue().replaceAll("\\.", "/"));
            }
        }                
        webChart.categories = categoryList;
        
        // do implementation-specific configuration
        webChart.configure(chartElement, timChartStyles);
        
        return webChart;

        // return the new WebChart object
        // return new WebChart(chartId, chart, paragraphTitle, description, imageXPixels, imageYPixels, categoryList);
    }
    
//    /**
//     * Constructor.
//     * 
//     * @param id the chart id number
//     * @param wrappedChart the chart object
//     * @param title the title of the paragraph
//     * @param description the description of the chart
//     * @param xPixels the image size in pixels (x axis)
//     * @param yPixels the image size in pixels (y axis)
//     * @param categoryList the categories under which to file the chart
//     */
//    public WebChart(final String id,
//                    final Object wrappedChart,
//                    final String title,
//                    final String description,
//                    final int xPixels,
//                    final int yPixels,
//                    final List<String> categoryList) {
//        chartId = id;
//        chart = wrappedChart;
//        paragraphTitle = title;
//        graphDescription = description;
//        imageXPixels = xPixels;
//        imageYPixels = yPixels;
//        categories = categoryList;
//    }
    
    /**
     * Substitutes the member name in JFree-level relevant fields.
     * @param the name of the member chart
     */
    void subMemberName(String memberName) {
        paragraphTitle = paragraphTitle.replace("$CHART_NAME$", memberName);
        graphDescription = graphDescription.replace("$CHART_NAME$", memberName);
        for (int i = 0; i < categories.size(); i++) {
            categories.set(i, categories.get(i).replace("$CHART_NAME$", memberName));
        }
        chartSubMemberName(memberName);
    }
    
    /**
     * Writes the necessary files to the web directory. 
     * 
     * @param webHome the home web directory where the images and html directories are
     * @param imageDirName the directory in which to write the images
     * @param webDirName the directory in which to write the HTML fragments
     * @param deployHome the absolute directory where the charts and html should be deployed under
     * @throws IOException error in writing image or html to disc
     */
    public final void deploy(final String webHome, final String deployHome, final String imageDirName, final String webDirName) throws IOException {
        
        //deploy the images to the web, in all the relevant directories
        deployImage(deployHome, imageDirName);            
         
        //deploy the html fragments to the web, in all the relevant directories        
        deployHTML(webHome, deployHome, webDirName, imageDirName);
        
    }
    
    /**
     * Creates and deploys the chart image to all the relevant directories.
     * These are obtained by adding the categories to the directory parameter.
     * 
     * @param deployHome the web home directory containing image and html folders
     * @param imageDirName the name of the directory where the images should be saved
     * @throws IOException image file could not be written to disc
     */
    private void deployImage(final String deployHome, final String imageDirName) throws IOException {
        
        //the parent directory of all the images
        File imageDir = new File(deployHome, imageDirName); 
        
        byte[] image = this.returnImage();
        
        try {
            //iterate through all the categories for this chart
            Iterator<String> it = categories.iterator();
            
            while (it.hasNext()) {
                File writeDirectory = new File(imageDir, it.next());
                
                if (logger.isDebugEnabled()) {
                    logger.debug("deloying chart image (id = " + chartId + ") to directory " + writeDirectory.toString());
                }
                
                //create directory if it does not exist
                if (!writeDirectory.exists()) {
                    FileUtils.forceMkdir(writeDirectory);
                }                
                
                //write file to output
                FileOutputStream streamOut = new FileOutputStream(new File(writeDirectory, CHART_FILE_NAME + chartId + "." + this.getImageFormat()));
                streamOut.write(image);
                streamOut.close();
            }
        } catch (IOException ioEx) {
            logger.error("error in writing image to disc for chart with id " + chartId);
            throw ioEx;
        }
    }
    
    /**
     * For members of a chart collection, sets the WebChart level parameters from the collection ones.
     * @param memberName
     * @param collection
     */
    void setGlobalParameters(String memberName, WebChartCollection collection) {
        
        // make new copy of collection object for this chart (will substitute name in these, so need distinct copy)
        categories = new ArrayList<String>();
        Iterator<String> it = collection.categoryList.iterator();
        while (it.hasNext()) {
            String currentCategory = (String) it.next();
            categories.add(currentCategory);
        }
        
        graphDescription = collection.description;
        paragraphTitle = collection.paragraphTitle;
        imageXPixels = collection.imageXPixels;
        imageYPixels = collection.imageYPixels;
        chartId = collection.chartIdPrefix + "_" + memberName;
    }
    
    /**
     * Creates and writes the HTML fragment to the web directory.
     * 
     * @param deployHome the absolute directory containing image and html folders
     * @param webDirName the directory to which the HTML fragments should be written
     * @param imageDirName the directory in which the images are found
     * @param webHome the home web directory where the images and html directories are
     * @throws IOException error in writing image html file to disk
     */
    private void deployHTML(final String webHome, final String deployHome, final String webDirName, final String imageDirName) throws IOException {    
        try {
            //the parent directory of all the html fragments
            File webDir = new File(deployHome, webDirName);
            
            //iterate through all categories
            Iterator<String> it = categories.iterator();
            while (it.hasNext()) {
                String category = it.next();
                
                File writeDirectory = new File(webDir, category);
                
                if (logger.isDebugEnabled()) {
                    logger.debug("deploying the chart html (id = " + chartId + ") to the directory " + writeDirectory.toString());
                }
                
                //create directory if it does not exist
                if (!writeDirectory.exists()) {
                    FileUtils.forceMkdir(writeDirectory);
                }
                
                File outputFile = new File(writeDirectory, HTML_FRAG_NAME + chartId + ".html");
                
                //write the html to disc
                PrintWriter out = new PrintWriter(new FileWriter(outputFile));
                if (paragraphTitle != "") {
                    out.println("<h2>");
                    out.println(paragraphTitle);
                    out.println("</h2>");
                }
                
                out.println("<img src=\"/" + webHome + "/" + imageDirName + "/" + category + "/" + CHART_FILE_NAME + chartId + ".png\" />");
                
                if (graphDescription != "") {
                    out.println("<p>");
                    out.println(graphDescription);
                    out.println("</p>");
                }
                
                out.close();
            }
            
        } catch (IOException ioEx) {
            logger.error("error in writing chart html file to disc for chart with id " + chartId);
            throw ioEx;
        }
    }


}
