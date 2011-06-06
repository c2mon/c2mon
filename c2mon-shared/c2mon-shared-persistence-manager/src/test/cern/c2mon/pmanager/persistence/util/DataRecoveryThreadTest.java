package cern.c2mon.pmanager.persistence.util;
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
//package ch.cern.tim.persistence.util;
//
//import junit.framework.TestCase;
//import ch.cern.tim.mock.AlarmListenerImpl;
//import ch.cern.tim.mock.DBHandlerImpl;
//import ch.cern.tim.mock.FallbackImpl;
//import ch.cern.tim.persistence.impl.TimPersistenceManager;
//
///**
// * Tests the ch.cern.tim.persistence.DataRecoveryThread class
// * A fallback file with data has to be provided to successfully run the test
// * 
// * @author mruizgar
// *
// */
//public class DataRecoveryThreadTest extends TestCase { 
//
//        /** Instance of the persistenceManager needed by this class*/
//        private TimPersistenceManager persistenceManager;
//                
//        /**
//         * Sets up the class attributes with the data needed for the test
//         */
//        public final void setUp() {
//            persistenceManager = new TimPersistenceManager(new DBHandlerImpl(), "./ShortTermLogFallback", new AlarmListenerImpl(), new FallbackImpl()); 
//        }
//        
//        /**
//         * Tests the class' run method
//         */
//        public final void testRun() {
//            (new DataRecoveryThread(this.persistenceManager)).run();
//            assertTrue(persistenceManager.getFallbackManager().isFallbackFileEmpty());
//        }
//        
//    
//}
