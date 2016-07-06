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
package cern.c2mon.pmanager.persistence.impl;

import java.util.ArrayList;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.mock.AlarmListenerImpl;
import cern.c2mon.pmanager.mock.DBHandlerImpl;
import cern.c2mon.pmanager.mock.FallbackImpl;
import cern.c2mon.pmanager.persistence.impl.TimPersistenceManager;
import junit.framework.TestCase;

/**
 * /**
 * JUnit test for the TimPersistenceManager class
 *  
 * @author mruizgar
 *
 */
 
public class TimPersistenceManagerTest extends TestCase {

    /** Instance of the class we want to test */
    private TimPersistenceManager timPersistenceManager;
    
    /** It sets all the objects needed for running the test */
    public final void setUp() {
        timPersistenceManager = new TimPersistenceManager(new DBHandlerImpl(), "./ShortTermLogFallback", new AlarmListenerImpl(), new FallbackImpl());
        
    }
    
    /**
     * Tests the storeData([Collection]) method
     */
    public final void testStoreObjectData() {
        int lines = timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        
        IFallback fallback = new FallbackImpl();
        timPersistenceManager.storeData(fallback);
        assertEquals(lines, timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
    }
    
    /**
     * Tests the storeData(IFallback) method
     */
    public final void testStoreListData() {
        int lines = timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        ArrayList data = new ArrayList();
        for (int i = 0; i < 4; i++) {
            data.add(new FallbackImpl());
        }
        timPersistenceManager.storeData(data);
        assertEquals(lines, timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
    }
    
    /**
     * Tests the storeData(IFallback) method when writing to the DB fails
     */
    public final void testStoreObjectConnectionFails() {
        FallbackImpl fallback = new FallbackImpl();
        int lines = timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        
        fallback.setObjectData(FallbackImpl.ERROR);
        timPersistenceManager.storeData(fallback);
        assertEquals(1, timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
    }
    
    /**
     * Tests the behavior of the storeData([Collection]) method when writing to the DB fails
     */
    public final void testStoreListDataConnectionFails() {
        ArrayList data = new ArrayList();
        int lines = timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        for (int i = 0; i < 4; i++) {
            data.add(new FallbackImpl());
        }
        FallbackImpl fallback = new FallbackImpl();
        fallback.setObjectData(FallbackImpl.ERROR);
        data.add(fallback);
        timPersistenceManager.storeData(data);
        assertEquals(5, timPersistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
    }
    
    
}
