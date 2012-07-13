package cern.c2mon.statistics.web;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

public class UserRequests {
    
    @Value("${c2mon.statistics.web.html.dir}")
    private File htmlDir;
    
    private String chosenDirectory;
    
    private List fragmentFiles;
    
    private FileFilter fileFilter;    
        
    /**
     * Setter method.
     * 
     * @param chosenDirectory the chosenCategory to set
     */
    public void setChosenDirectory(String chosenDirectory) {
        
        this.chosenDirectory = chosenDirectory;
        
        //update fragment files
        fragmentFiles = new ArrayList();
        fileFilter = new OnlyFiles();
        File absChosenDirectory = new File(htmlDir,chosenDirectory);
        File[] chartFiles = absChosenDirectory.listFiles(fileFilter);
        List fileNames = new ArrayList();
        for (int i = 0; i < chartFiles.length; i++) {
            fileNames.add("/" + htmlDir.getName() + "/" + chosenDirectory + chartFiles[i].getName());
        }
//        List files = Arrays.asList(chartFiles);
        fragmentFiles.addAll(fileNames);
    }

    /**
     * @return the fragmentFiles
     */
    public List getFragmentFiles() {
        return fragmentFiles;
    }

    /**
     * @return the chosenDirectory
     */
    public String getChosenDirectory() {    
        return chosenDirectory;
    }
}
