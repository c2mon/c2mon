package cern.c2mon.statistics.generator;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cern.c2mon.statistics.generator.charts.WebChart;
import cern.c2mon.statistics.generator.charts.WebChartCollection;
import cern.c2mon.statistics.generator.exceptions.GraphConfigException;
import cern.c2mon.statistics.generator.exceptions.InvalidTableNameException;

/**
 * The main class of the chart and table deployer package.
 * 
 * Contains the main method that should be run once the new
 * statistics have been generated in the database.
 * 
 * Can always be run manually to refresh the graphs (if for some reason
 * the cron job was not performed, for example).
 * 
 * @author mbrightw
 *
 */
public class GraphDeployer {

    /**
     * Name of the file used to lock access to the charts directory
     * (resides in top chart directory).
     */
    private static final String LOCK_FILE_NAME = "charts.lck";
    
    /**
     * The minimum number of characters that the image, html and deploy directories must have
     * (enforced to avoid deleting other directories by mistake).
     */
    private static final short MIN_DIR_LENGTH = 5;
    
    /**
     * The log4j process logger.
     */
    private static Logger logger;
      
    /**
     * The deployment system directory. Must be at least MIN_DIR_LENGTH characters long.
     */
    private String deployHome;
    
    /**
     * The web home on the web server (where the charts and html directories are).
     */
    private String webHome;
    
    /**
     * The name of the directory where the chart images should be written to
     * in deployHome. Must be at least MIN_DIR_LENGTH characters long.
     */
    private String imageDirName = null;
    
    /**
     * The name of directory where the html fragments should be written to
     * in deployHome. Must be at least MIN_DIR_LENGTH characters long.
     */
    private String htmlDirName = null;
    
    /**
     * The list of charts to be deployed to the web 
     * (charts together with description etc.)
     */
    private ArrayList<WebChart> webCharts = new ArrayList<WebChart>();
    //Collection<Tables> tables;
    
    /**
     * Reference to the collection of standard styles for TIM charts.
     */
    private TimChartStyles timChartStyles;
    
    /**
     * Default constructor.
     */
    GraphDeployer() {
    }
    
    /**
     * Constructs the main graph deployer object from the configuration XML document.
     * @param graphXMLDocument the XML document specifying the graphs and tables
     */
    final void configure(final Document graphXMLDocument) {
        //get the TIM chart style information
        timChartStyles = TimChartStyles.fromXML(graphXMLDocument);
        
        //get the charts in the document
        NodeList chartElements = graphXMLDocument.getElementsByTagName("chart");
        int listLength = chartElements.getLength(); 
        logger.info(listLength + " charts found in configuration file");
        //iterate through all the chart elements in the XML document
        for (int i = 0; i < listLength; i++) {
            Element chartElement = (Element) chartElements.item(i);
            //try to construct the WebChart object from the XML
            WebChart webChart = null;
            try {
                webChart = WebChart.fromXML(chartElement, timChartStyles);
                //add this WebChart to the global list
                webCharts.add(webChart);
            } catch (GraphConfigException ex) {
                //if configuration not recognized, skip the graph
                logger.warn("the configuration of one of the graphs was not recognized: " + ex);
                logger.warn("skipping this graph...");
                System.err.println("GraphConfigException caught while processing XML config document.");
                System.err.println("Skipping this graph. It will no longer be available for display. See log file.");
                ex.printStackTrace();
                continue;
            } catch (SQLException sqlEx) {
                //if SQL Exception occurs, skip graph and notify by email
                logger.warn("SQL exception caught while generating graph: " + sqlEx);
                logger.warn("skipping this graph and sending notification...");
                System.err.println("SQL exception caught while generating graph. See log file for details.");
                sqlEx.printStackTrace();
                continue;
            } catch (NullPointerException nullEx) {
                //probably because some XML field was not present
                logger.warn("NullPointerException caught while processing XML config document: "  + nullEx);
                logger.warn("This is probably due to a missing XML field in the configuration file.");
                System.err.println("NullPointerException caught while processing XML config document. See log file.");
                nullEx.printStackTrace();
            } catch (InvalidTableNameException tableEx) {
                //if bad table name used, stop the package to prevent SQL injection errors
                logger.fatal("Detected table name with unauthorized characters (non-alphanumeric + _");
                logger.fatal("Terminating...");
                System.err.println("Detected table name with unauthorized characters (non-alphanumeric + _");
                System.err.println("Statistics generator was terminated.");
                tableEx.printStackTrace();
                throw new RuntimeException(tableEx);
            }         
        }
        logger.info(webCharts.size() + " charts correctly configured");
        
        //get the chart groups in the XML document
        NodeList chartGroupElements = graphXMLDocument.getElementsByTagName("chart-group");
        listLength = chartGroupElements.getLength(); 
        logger.info(listLength + " charts groups found in configuration file");
        //iterate through all the chart group elements in the XML document
        for (int i = 0; i < listLength; i++) {
            Element chartGroupElement = (Element) chartGroupElements.item(i);
            //try to construct the WebCharts object from the XML
            WebChartCollection webChartCollection = null;
            try {
                webChartCollection = WebChartCollection.fromXML(chartGroupElement, timChartStyles);
                webCharts.addAll(webChartCollection.getWebCharts());
            } catch (GraphConfigException ex) {
                //if configuration not recognized, skip the chart collection
                logger.warn("the configuration of one of the charts in a chart collection was not recognized: " + ex);
                logger.warn("skipping this chart...");
                System.err.println("GraphConfigException caught while processing XML config document.");
                System.err.println("Skipping this chart collection. It will no longer be available for display. See log file.");
                ex.printStackTrace();
                continue; 
            } catch (SQLException sqlEx) {
                //if SQL Exception occurs, skip graph and notify by email
                logger.warn("SQL exception caught while generating chart in collection: " + sqlEx);
                logger.warn("skipping this chart and sending notification...");
                System.err.println("SQL exception caught while generating chart in collection. See log file for details.");
                sqlEx.printStackTrace();
                continue;           
            } catch (InvalidTableNameException tableEx) {
                //if bad table name used, stop the package to prevent SQL injection errors
                logger.fatal("Detected table name with unauthorized characters (non-alphanumeric + _");
                logger.fatal("Terminating...");
                System.err.println("Detected table name with unauthorized characters (non-alphanumeric + _");
                System.err.println("Statistics generator was terminated.");
                tableEx.printStackTrace();
                throw new RuntimeException(tableEx);
            }    
            
        }
        
            
        
        //get the tables in the document
    }
    
    /**
     * Deploys the charts and tables to the correct directory.
     * 
     * If an error in writing to the disc occurs, the deployment is interrupted and
     * an email notification is sent.
     */
    public final void deploy() {
        //deploy the charts to the web
        logger.info("deploying the charts to the web directories");
        try {
            deployCharts();
        } catch (IOException ioEx) {
            //if any IOException caught, stop execution immediately as serious problem accessing disc
            logger.fatal("IOException caught in writing charts or html to disc: " + ioEx);
            ioEx.printStackTrace();
            logger.fatal("exiting...");
            throw new RuntimeException(ioEx);
        }
                  
        //deploy the tables to the web -- tables not implemented
        //deployTables();
    }
    
    /**
     * Deploys all the charts (images and html) to the web directories. 
     * 
     * @throws IOException error in writing one of the charts to disc
     */
    private void deployCharts() throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("entering deployCharts()...");
        }
        //iterate through the charts
        Iterator<WebChart> it = webCharts.iterator();
        while (it.hasNext()) {
            //deploy the (non-null) charts (image + HTML) to web location
            //(could be null if chart type not recognized)
            WebChart currentWebChart = it.next();
            if (currentWebChart.canDeploy()) {
                currentWebChart.deploy(webHome, deployHome, imageDirName, htmlDirName); 
            }          
        }
        if (logger.isDebugEnabled()) {
            logger.debug("...leaving deployCharts()");
        }
    }
    
    /**
     * Removes all the current charts and html directories (if they exist) 
     * and recreates them (to remove old charts). Must be called within a file lock.
     */
    private void renewDirectories() {                      
        try {                        
            logger.info("cleaning old directories");        
            //remove image and html directories
            File imageDir = new File(deployHome, imageDirName);
            File htmlDir = new File(deployHome, htmlDirName);
            if (imageDir.exists()) {
                FileUtils.deleteDirectory(imageDir);           
            }
            if (htmlDir.exists()) {
                FileUtils.deleteDirectory(htmlDir);          
            }
            
            //recreate them
            if (!htmlDir.mkdir()) {
              throw new IOException("Error in creating HTML directory.");
            }
            if (!imageDir.mkdir()) {
              throw new IOException("Error in creating image directory.");
            }            
            
        } catch (IOException ioEx) {
            logger.fatal("IOException caught when removing deploy directories: " + ioEx.getMessage());
            ioEx.printStackTrace();
            throw new RuntimeException(ioEx);
        }        
    }
    
    /**
     * The main method of the graph deployer package. When run, it generates
     * the graphs from the database, saves the images, and creates and saves
     * the html fragments for displaying on the web.
     * 
     * It takes the following arguments:
     * 
     * -w, --webhome        the web home on the web server (where the charts and html directories are)
     * -d, --deployhome     the top directory where the charts and html should be deployed under
     * -i, --imagedir       the directory where the generated images are written to in webhome
     * -c, --graphconfig    the XML file containing the web descriptions
     * -h, --htmldir        the directory where the html fragments are written to in webhome
     * -l, --log4j          the log4j configuration file
     * -R                   remove all old contents of html and image directories at runtime !!handle with care!!
     * 
     * @param args takes the arguments deployhome, imagedir, graphconfig, htmldir, log4j
     */
    public static void main(final String[] args) {
        
        boolean cleanDirectories = false;
        
        logger = Logger.getLogger(GraphDeployer.class);
        
        GraphDeployer deployer = new GraphDeployer();
        
        // the command line options    
        Option wOption = new Option("w", "webhome", true, "the web home on the web server (where the charts and html directories are)");
        wOption.setRequired(true);
        Option dOption = new Option("d", "deployhome", true, "the top directory where the charts and html should be deployed under");
        dOption.setRequired(true);
        Option iOption = new Option("i", "imagedir", true, "the directory to write the graphs to in webhome");
        iOption.setRequired(true);
        Option cOption = new Option("c", "graphconfig", true, "the XML file containing the chart descriptions");
        cOption.setRequired(true);
        Option hOption = new Option("h", "htmldir", true, "the directory to write the html fragments to in webhome");
        hOption.setRequired(true);
        Option lOption = new Option("l", "log4j", true, "the log4j XML configuration file");
        lOption.setRequired(true);
        Option rOption = new Option("R", false, "remove all old content of html and image directories (clean option) !!handle with care!!");
        rOption.setRequired(false);
        
        Options options = new Options();
        options.addOption(wOption);
        options.addOption(dOption);
        options.addOption(iOption);
        options.addOption(cOption);
        options.addOption(hOption);
        options.addOption(lOption);
        options.addOption(rOption);

        // the command line option Strings
        
        String chartConfigLocation = null;
        String loggerConfig = null;
        
        // try to parse the commandline
        try {
            CommandLineParser parser = new GnuParser();
            CommandLine cmd = parser.parse(options, args);
            deployer.deployHome = cmd.getOptionValue("d");
            deployer.imageDirName = cmd.getOptionValue("i");
            deployer.htmlDirName = cmd.getOptionValue("h");
            deployer.webHome = cmd.getOptionValue("w");
            chartConfigLocation = cmd.getOptionValue("c");
            loggerConfig = cmd.getOptionValue("l");
            cleanDirectories = cmd.hasOption("R");
            
        } catch (ParseException e) {  // parsing fails
            System.err.println("Error in parsing the command line arguments.");           
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("GraphDeployer", options);
            e.printStackTrace();
            System.exit(1);
        }
        
        if (deployer.deployHome.length() < MIN_DIR_LENGTH 
              || deployer.imageDirName.length() < MIN_DIR_LENGTH  
              || deployer.htmlDirName.length() < MIN_DIR_LENGTH) {
            System.err.println("deploy, image and html directory names must be at least 5 letters long");
            System.exit(1);
        }
        
        // set the name of the process
        System.setProperty("tim.process.name", "GraphDeployer");
        
        // try to configure the logger
        try {
            // Load log4j xml file
            DOMConfigurator.configureAndWatch(loggerConfig);
            if (logger.isInfoEnabled()) {
                logger.info("[preDeploy] Configured log4j from " + loggerConfig);
            }
        } catch (Exception ex) {   // logger configuration fails
            logger.fatal("Unable to load log4j configuration file : " + ex.getMessage());
            logger.fatal("exiting...");
            ex.printStackTrace();
            System.exit(1);
        }
        
        //parse graph XML file
        Document graphXMLDocument;
        DOMParser parser = new DOMParser();
        try {
            parser.parse(chartConfigLocation);
            graphXMLDocument =  parser.getDocument();
            deployer.configure(graphXMLDocument);
        } catch (IOException ioEx) {
            logger.fatal("Graph configuration file could not be read: " + ioEx);
            ioEx.printStackTrace();
            logger.fatal("exiting...");
            System.exit(1);
        } catch (org.xml.sax.SAXException saxEx) {
            logger.fatal("Error in parsing web configuration XML document: " + saxEx);
            saxEx.printStackTrace();
            logger.fatal("exiting...");
            System.exit(1);
        } catch (Exception otherEx) {
            //exit and notify if any other exception is caught
            logger.fatal("Unidentified exception caught: " + otherEx);
            otherEx.printStackTrace();
            logger.fatal("exiting...");
            System.exit(1);
        }
        
        //deploy the graph collection to the web
        logger.info("deploying statistics to the web");
        
        //lock access to these directories using the lock file,
        //since the web application also accesses them                    
        FileLock lock = null;
        RandomAccessFile lockFileRandom = null;
        
        try {
          logger.debug("obtaining file lock...");
          
          //lock access            
          File lockFile = new File(deployer.deployHome, GraphDeployer.LOCK_FILE_NAME);                                                             
          lockFileRandom = new RandomAccessFile(lockFile, "rw"); 
          FileChannel channel = lockFileRandom.getChannel();
          lock = channel.lock();
          
          //clean and deploy
          if (cleanDirectories) {
            deployer.renewDirectories();
          }  
          deployer.deploy();
          
        } catch (IOException ioEx) {          
          logger.error("IOException caught while cleaning and deploying new charts: " + ioEx.getMessage());
          ioEx.printStackTrace(System.err);
          throw new RuntimeException(ioEx);
          
        } finally {
          //release lock and lock file if they were set
          if (lock != null) {
            try {
              lock.release();
            } catch (IOException ioEx) {
              logger.error("IOException caught while unlocking chart directory: " + ioEx.getMessage());
              ioEx.printStackTrace();
            }
          }
          if (lockFileRandom != null) {
            try {
              lockFileRandom.close();
            } catch (IOException ioEx) {
              logger.error("IOException caught while closing lock file access: " + ioEx.getMessage());
              ioEx.printStackTrace();
            }
          }
        }
        

    }

}
