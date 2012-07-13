package cern.c2mon.statistics.web;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * Fetches and stores the list of directories under the "DAQ" directory. This list is used to generated the
 * HTML selection boxes on the web page. The update procedure fetches the current directories and is called
 * by the ChartManager once the latter has imported the latest charts.
 * 
 * @author mbrightw
 *
 */
public class DaqList {

    private static Logger logger = Logger.getLogger(DaqList.class);
    
    //import Spring properties
    @Value("${c2mon.statistics.web.html.dir}")
    private String htmlDir;
    @Value("${c2mon.statistics.web.deploy.dir}")
    private String deployDir;
    @Value("${c2mon.statistics.web.daq.dir}")
    private String daqDir;
    @Value("${catalina.home}")
    private String catalinaHome;

    /**
     * List of the DAQs in the DAQ chart directory.
     */
    private List<String> daqList;
    
    /**
     * List containing DAQ_NAME -> DAQ_DIR/DAQ_NAME/ for select menu.
     */
    private List<SelectItem> selectList;
            
    /**
     * Default constructor.
     */
    public DaqList() {
    }
    
    /**
     * Update the DaqList from the current directory. Called by the chartManager within a file lock.
     */
    public final void update() {
        //get daq list
        logger.debug("fetching the DAQ directories");
        File daqDirFile = new File(catalinaHome + "/" + deployDir, htmlDir + "/" + daqDir);       
        File[] files = daqDirFile.listFiles();
        
        //if the directory does not exist, set the array to be empty;
        //web page will need refreshing once charts are available again
        //(this should only happen if there are no charts in the shared
        // chart directory)
        if (files == null) {
            logger.warn("The DAQ chart directory is empty on the web server! Page will need refreshing.");
            files = new File[0];
        }
        
        List<File> fileList = Arrays.asList(files);        
        Collections.sort(fileList);
        daqList = new ArrayList<String>();
        selectList = new ArrayList<SelectItem>();
        Iterator<File> it = fileList.iterator();
        while (it.hasNext()) { 
            File currentFile = it.next();
            daqList.add(currentFile.getName());
            selectList.add(new SelectItem(daqDir + "/" + currentFile.getName() + "/", currentFile.getName()));
        }
    }

    /**
     * Getter.
     * @return the list of DAQs in directory structure
     */
    public final List<String> getDaqList() {
        return daqList;
    }

    /**
     * Setter.
     * @param daqList the list of DAQs
     */
    public final void setDaqList(final List<String> daqList) {
        this.daqList = daqList;
    }

    /**
     * Getter.
     * @return the list of DAQ directories
     */
    public final List<SelectItem> getSelectList() {
        return selectList;
    }

    /**
     * Setter.
     * @param selectList the list of DAQ directories
     */
    public final void setSelectList(final List<SelectItem> selectList) {
        this.selectList = selectList;
    }

}
