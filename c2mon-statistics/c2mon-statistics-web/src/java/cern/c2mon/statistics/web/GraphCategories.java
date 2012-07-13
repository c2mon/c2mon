package cern.c2mon.statistics.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.faces.model.SelectItem;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * Spring bean containing the list of chart categories that are currently being used.
 * Is updated every time new charts are imported into the web application.
 *  
 * @author mbrightw
 *
 */
public class GraphCategories {
    
    
    /**
     * The Logger.
     */
    private static Logger logger = Logger.getLogger(DaqList.class);    
    
    //import Spring properties
    @Value("${c2mon.statistics.web.html.dir}")
    private String htmlDir;
    @Value("${c2mon.statistics.web.deploy.dir}")
    private String deployDir;
    @Value("${catalina.home}")
    private String catalinaHome;
    
    /**
     * Contains the different chart categories. The keys appear in the web menu
     * and the values are the directories.
     */
    private List<SelectItem> categories;
    
    /**
     * Default constructor.
     * Initialises directory list by accessing file system.
     */
    public GraphCategories() {                                
    }
    
    /**
     * Updates the object with the new charts. Called by the chartManager within a file lock.
     */
    public final void update() {
        logger.debug("fetching the graph categories");        
        File htmlDirFile = new File(catalinaHome + "/" + deployDir, htmlDir);
        htmlDir = htmlDirFile.toString();
        
        categories = new ArrayList<SelectItem>();
        List categoryList;
        DirectoryFetcher directoryFetcher = new DirectoryFetcher(htmlDir);
        try {
            categoryList = directoryFetcher.fetch();
            Collections.sort(categoryList);
            Iterator it = categoryList.iterator();
            while (it.hasNext()) {            
                String currentCat = (String) it.next(); 
                
                //remove last '/' and replace others with '.'
                String noSlash = currentCat.substring(0, currentCat.length() - 1);
                String withDots = noSlash.replaceAll("/", ".");
                
                //add to hash map, with "dot" version pointing to directory version
                categories.add(new SelectItem(currentCat, withDots));
        }
        } catch (IOException e) {
            logger.error("IOException caught while updating the graphCategory object: " + e.getMessage());            
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Getter method.
     * 
     * @return the map of chart categories
     */
    public final List getCategories() {
        return categories;
    }
    
    /**
     * For testing only
     * 
     * @param args none needed
     */
    public static void main(final String[] args) {
        GraphCategories gc = new GraphCategories();
        System.out.println(gc.categories);
    }
    
}
