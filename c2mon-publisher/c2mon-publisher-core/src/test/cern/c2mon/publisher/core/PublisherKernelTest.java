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
package cern.c2mon.publisher.core;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.publisher.test.TestPublisher;

public class PublisherKernelTest {

  @Before
  public void beforeTest() {
    System.setProperty("app.name", "C2MON-PUBLISHER-CORE-TEST");
    System.setProperty("app.version", "1.0");
    System.setProperty("c2mon.client.conf.url", "http://timweb/conf/c2mon-client.properties");
    System.setProperty("log4j.configuration", "cern/c2mon/publisher/core/log4j.xml");
    System.setProperty("c2mon.publisher.tid.location", "src/test/cern/c2mon/publisher/core/test.tid");
  }
  
  @Test
  public void testStartup() {
    PublisherKernel.main(new String[]{});
    
    try {Thread.sleep(3000);} catch (InterruptedException e) {}
    
    assertTrue(TestPublisher.getUpdateCounter() >= 9);
  }
}
