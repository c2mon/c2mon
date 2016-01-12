/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
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
