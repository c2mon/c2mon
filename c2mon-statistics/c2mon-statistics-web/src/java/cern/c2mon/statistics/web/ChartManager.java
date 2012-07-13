package cern.c2mon.statistics.web;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Date;

import javax.annotation.Resource;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import cern.c2mon.statistics.web.exceptions.NoChartsException;

/**
 * The class that makes sure the most recent images are 
 * available in the web directory. These are copied from the directory
 * where the generator puts them into the web directory for access by
 * the web application.
 * 
 * Loaded in Spring context.
 * 
 * @author mbrightw
 *
 */
public class ChartManager extends Timer {

    /**
     * Name of the file used to lock access to the charts directory.
     */
    private static final String LOCK_FILE_NAME = "charts.lck";
    
    /**
     * The time between successive checks for new charts (in milliseconds).
     */
    private static final int CHECK_FREQUENCY = 1800000; //every 30 minutes in production   
    
    /**
     * Start-up delay before checking the chart directories (in milliseconds).
     */
    private static final int START_DELAY = 10000;   
    
    //import Spring properties
    @Value("${c2mon.statistics.web.image.dir}")
    private String imageDir;
    @Value("${c2mon.statistics.web.html.dir}")
    private String htmlDir;
    @Value("${c2mon.statistics.web.deploy.dir}")
    private String deployDir;
    @Value("${c2mon.statistics.web.generator.dir}")
    private String generatorDir;
    @Value("${catalina.home}")
    private String catalinaHome;
    
    /**
     * Reference to list of DAQs in DAQ directory
     * (used for DAQ charts pages and menu)    
     */
    @Resource
    private DaqList daqList;
    
    /**
     * Reference to the list of charts categories
     * (used for generic chart pages and menu).
     */
    @Resource
    private GraphCategories graphCategories;
    
    /**
     * Logger.
     */
    private static Logger logger = Logger.getLogger(ChartManager.class);
    
    /**
     * The date the current graphs were created.
     */
    private String creationDate;

    /**
     * The top charts directory.
     */
    private File lockFile;
    
    /**
     * The directory where the images are deployed on the web.
     */
    private File deployImageDir;
    
    /**
     * The directory where the html fragments are deployed on the web.
     */
    private File deployHtmlDir;
    
    /**
     * The directory where the generated images are saved.
     */
    private File generatorImageDir;
    
    /**
     * The directory where the generated html fragments are held.
     */
    private File generatorHtmlDir;
           
    /**
     * Default constructor.
     */
    public ChartManager()  {        
        //construct the timer 
        super(true); //true = is deamon thread                              
    }

    /**
     * Updates the charts if new ones have been created by the generator process. These are then
     * imported into the web directory.       
     * @throws NoChartsException if image or html directories do not exist
     * @throws IOException if error in copying images, deleting directories, or handling lock file
     */
    private void update() throws NoChartsException, IOException {
   
        logger.debug("checking web charts are up to date...");
        //lock the top chart directory before accessing (to avoid conflict with generator process)
        FileLock lock = null;
        RandomAccessFile randomLockFile = null;
        try {
            randomLockFile = new RandomAccessFile(lockFile, "rw"); 
            FileChannel channel = randomLockFile.getChannel();
            lock = channel.lock();
            //if image or html directories do not exit, then throw an exception (no statistics available)
            if (!generatorImageDir.exists() || !generatorHtmlDir.exists()) {
                throw new NoChartsException();
            }
            
            //if the generator directories are more recent than the deploy ones,
            //then deploy the generator directories (use html dirs here, could use 
            //image dirs instead)
            //if necessary, remove the deploy directories first
            if (!deployHtmlDir.exists() || !deployImageDir.exists()
                    || generatorHtmlDir.lastModified() > deployHtmlDir.lastModified()) {
                logger.debug("copying new charts into the web directory.");
                if (deployHtmlDir.exists()) {
                    FileUtils.deleteDirectory(deployHtmlDir);
                }
                if (deployImageDir.exists()) {
                    FileUtils.deleteDirectory(deployImageDir);
                }
                FileUtils.copyDirectory(generatorImageDir, deployImageDir); //preserves creation date
                FileUtils.copyDirectory(generatorHtmlDir, deployHtmlDir);
                
                //set the creation date of the current charts
                Date created = new Date(generatorHtmlDir.lastModified());
                creationDate = created.toString();
                
                //update the current list of DAQs (in the DAQ directory)
                daqList.update();
                //update the general chart category object
                graphCategories.update();
                
            } else {
              logger.debug("charts are up to date - no update needed.");
            }                       
            
        } finally {
            if (lock != null) {
              try {
                lock.release();
              } catch (IOException ioEx) {
                logger.error("IOException caught while unlocking chart directory: " + ioEx.getMessage());
                throw ioEx;
              }
            }
            if (randomLockFile != null) {
              try {
                randomLockFile.close();
              } catch (IOException ioEx) {
                logger.error("IOException caught while closing lock file access: " + ioEx.getMessage());
                throw ioEx;
              }
              
            }
        }                       
    }
    
    /**
     * Bean init method. When created this bean updates the directories as
     * necessary. It starts up a timer which checks for new graphs every 30 minutes.
     * 
     * @throws NoChartsException if image or html directories do not exist
     * @throws IOException if error in copying images or deleting directories
     */
    public final void init() throws NoChartsException, IOException {        
        logger.debug("Initializing chart manager");
        lockFile = new File(generatorDir, ChartManager.LOCK_FILE_NAME);
        deployImageDir = new File(catalinaHome + "/" + deployDir, imageDir);
        logger.debug("images will be deployed to: " + deployImageDir.toString());        
        deployHtmlDir = new File(catalinaHome + "/" + deployDir, htmlDir);        
        logger.debug("chart html will be deployed to: " + deployHtmlDir.toString());
        generatorImageDir = new File(generatorDir, imageDir);
        logger.debug("images will be copied from: " + generatorImageDir.toString());
        generatorHtmlDir = new File(generatorDir, htmlDir);        
        logger.debug("chart html will be copied from: " + generatorHtmlDir.toString());
        
        //schedule the updates
        this.schedule(new UpdateTask(), ChartManager.START_DELAY, ChartManager.CHECK_FREQUENCY);
    }           
       
    /**
     * Getter method.
     * @return the creationDate
     */
    public final String getCreationDate() {
        return creationDate;
    }      

    /**
     * Setter.
     * @param daqList the daqList to set
     */
    public final void setDaqList(final DaqList daqList) {
        this.daqList = daqList;
    }
    
    /**
     * Setter method.
     * @param graphCategories the graphCategories to set
     */
    public final void setGraphCategories(final GraphCategories graphCategories) {
        this.graphCategories = graphCategories;
    }

    /**
     * Timer class that calls the update procedure.
     * @author mbrightw
     *
     */
    private class UpdateTask extends TimerTask {       
        
        /**
         * The task that is run at each tick of the clock.
         */
        public void run() {
            try {
                update();
            } catch (Exception e) {
                logger.error("Exception caught while updating web charts: " + e.toString());
                e.printStackTrace();
            }
        }
    } 
}
