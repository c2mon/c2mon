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
package cern.c2mon.client.apitest;

import org.junit.Test;

/**
 * Tests C2monTestClient starts up correctly, using the C2MON test system.
 * 
 * @author Mark Brightwell
 *
 */
public class C2monTestClientTest {

  @Test
  public void testSubscription() {
    System.setProperty("c2mon.client.test.tagids.location", "classpath:resources/c2mon-test-client-tagids.txt");
    System.setProperty("c2mon.client.test.subscription.number", "5");
    System.setProperty("c2mon.client.process.name", "test-client-tags");
    System.setProperty("c2mon.client.conf.url", "url:http://timweb/test/conf/c2mon-client.properties");
    String[] args = new String[0];
    C2monTestClient.main(args);
  }
  
}
