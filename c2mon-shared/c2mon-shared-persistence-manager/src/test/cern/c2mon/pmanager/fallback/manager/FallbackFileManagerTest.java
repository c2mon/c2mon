/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2009 CERN This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.pmanager.fallback.manager;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.FallbackProperties;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.pmanager.fallback.exception.SystemDiskSpaceException;
import cern.c2mon.pmanager.fallback.manager.FallbackFileManager;
import cern.c2mon.pmanager.mock.FallbackImpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;



import junit.framework.TestCase;

/**
 * JUnit test for the class
 * ch.cern.tim.server.log.fallback.DataTagFallbackHelper.java It tests that all
 * DataTagFallbackHelper methods are properly working.
 * 
 * @author mruizgar
 * 
 */
public class FallbackFileManagerTest extends TestCase {
   
    /** Log4j logger for the class*/ 
    private static final Logger LOG = Logger.getLogger(FallbackFileManagerTest.class);
    
    /** Number of lines to be read/deleted from the fallback log file*/
    private static final int NUMBER_OF_LINES = 3;

    /** List of DataTagCacheObjects */
    private ArrayList data;
    
    /** Absolute path for the fallback file */
    private static final String PATH = "./DataTagFallback.log";
    
    /** Instance to the FallbackFileManager class that we want to test*/
    private FallbackFileManager fFileManager;

    /**
     * Prepares the dataTags list that will be used for the tests
     */
    protected final void setUp() {
        data = new ArrayList();
        // create a FallbackImpl object
        FallbackImpl fImpl = new FallbackImpl();
        fImpl.setObjectData("100142 CP.MEY.TEST13:TEST_STATE    false   Boolean 2009-01-15 10:06:34.562 0   null    0   I   2009-01-15 10:08:10.8");
        data.add(fImpl);
        
        fImpl = new FallbackImpl();
        fImpl.setObjectData("100143  CP.MEY.TEST14:TEST_STATE    false   Boolean 2009-01-15 10:06:34.562 0   null    0   I   2009-01-15 10:08:11.235");
        data.add(fImpl);

        fFileManager = new FallbackFileManager(FallbackFileManagerTest.PATH, new FallbackImpl());
    }

    /**
     * Tests the fallback([Collection]) method. The test will be successful when
     * something is written in the fallback log file
     */
    public final void testFallBackList() {
        try {
           fFileManager.fallback(data);
        } catch (DataFallbackException e) {
            fail("Data could not be written to the log file " + e.getMessage());
        }
        boolean empty = fFileManager.isFallbackFileEmpty();
        assertFalse(empty);
    }

    /**
     * Test the fallBack() method. The test will be successful when
     * something is written in the fallback log file
     */
    public final void testFallBack() {
        boolean empty = true;

        try {
            fFileManager.fallback((IFallback) data.get(0));
            empty = fFileManager.isFallbackFileEmpty();
        } catch (DataFallbackException e) {
            fail("Data could not be written to the log file " + e.getMessage());
        }
        assertFalse(empty);
    }

    /**
     * Tests the readDataBack method. The test will be successful when IFallback object
     * are retrieved from the file TODO fix test
     */
//    public final void testReadDataBack() {
//        
//        List data;
//        
//        try {
//            data = fFileManager.readDataBack(NUMBER_OF_LINES);
//            assertFalse(data.size() == 0);
//        } catch (DataFallbackException e) {
//            LOG.error("Error while reading back the Fallback log " + e.getMessage());
//            fail("Error while reading back the Fallback log ");
//
//        }
//    }

    /**
     * Tests the RemoveReadData method. The test will be succesful when the
     * indicated number of rows are deleted from the fallback log file
     */ 
//    public final void testRemoveReadData() {
//        boolean removed = false;
//        removed = fFileManager.removeReadData(FallbackFileManagerTest.NUMBER_OF_LINES);
//        assertTrue(removed);
//    }
    
    /**
     * Tests the CheckDataTagsFileDiscSpace method. 
     */
    public final void testCheckDataTagsFileDiscSpace() {
        int nCheck = 100;
        try {
            fFileManager.setNextCheckFallbackData(nCheck);
            fFileManager.isDiskSpaceCheckDone(FallbackProperties.getInstance().getMinimunDiscFreeSpace());
       } catch (SystemDiskSpaceException e) {
           fail(e.getMessage());
       }
       assertTrue(fFileManager.getNextCheckFallbackData() == nCheck);
    }

}
