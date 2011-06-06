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
 * JUnit test for the ch.cern.tim.persistence.impl.TimPersistenceManager class
 *  
 * @author mruizgar
 *
 */
 
public class TimPersistenceManagerTest extends TestCase {

    /** Instance of the class we want to test */
    private TimPersistenceManager persistenceManager;
    
    /** It sets all the objects needed for running the test */
    public final void setUp() {
        persistenceManager = new TimPersistenceManager(new DBHandlerImpl(), "./ShortTermLogFallback", new AlarmListenerImpl(), new FallbackImpl());
        
    }
    
    /**
     * Tests the storeData([Collection]) method
     */
    public final void testStoreObjectData() {
        int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        
        IFallback fallback = new FallbackImpl();
        persistenceManager.storeData(fallback);
        assertEquals(lines, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
    }
    
    /**
     * Tests the storeData(IFallback) method
     */
    public final void testStoreListData() {
        int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        ArrayList data = new ArrayList();
        for (int i = 0; i < 4; i++) {
            data.add(new FallbackImpl());
        }
        persistenceManager.storeData(data);
        assertEquals(lines, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines());
    }
    
    /**
     * Tests the storeData(IFallback) method when writing to the DB fails
     */
    public final void testStoreObjectConnectionFails() {
        FallbackImpl fallback = new FallbackImpl();
        int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        
        fallback.setObjectData(FallbackImpl.ERROR);
        persistenceManager.storeData(fallback);
        assertEquals(1, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
    }
    
    /**
     * Tests the behavior of the storeData([Collection]) method when writing to the DB fails
     */
    public final void testStoreListDataConnectionFails() {
        ArrayList data = new ArrayList();
        int lines = persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines();
        for (int i = 0; i < 4; i++) {
            data.add(new FallbackImpl());
        }
        FallbackImpl fallback = new FallbackImpl();
        fallback.setObjectData(FallbackImpl.ERROR);
        data.add(fallback);
        persistenceManager.storeData(data);
        assertEquals(5, persistenceManager.getFallbackManager().getFallbackFileController().getNumberOfLines() - lines);
    }
    
    
}
