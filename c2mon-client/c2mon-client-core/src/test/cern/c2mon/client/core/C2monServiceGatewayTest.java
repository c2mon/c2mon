/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.core;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTagValue;

/**
 * Integration test of Client API modules.
 * 
 * @author Mark Brightwell
 *
 */
public class C2monServiceGatewayTest {

  /**
   * Log4j instance
   */
  private static final Logger LOG = Logger.getLogger(C2monServiceGatewayTest.class);
  
  @Test
  public void startClient() throws InterruptedException {
    C2monServiceGateway.startC2monClient();    
    assertNotNull(C2monServiceGateway.getCommandManager());
    assertNotNull(C2monServiceGateway.getSupervisionManager());
    assertNotNull(C2monServiceGateway.getTagManager());
  }
  
  /**
   * Needs running without JVM properties set in order to test correct loading
   * into Spring context.
   * @throws InterruptedException
   */
  @Test
  public void startClientWithProperties() throws InterruptedException {
    C2monServiceGateway.startC2monClient("classpath:cern/c2mon/client/core/test-properties.txt");    
    assertNotNull(C2monServiceGateway.getCommandManager());
    assertNotNull(C2monServiceGateway.getSupervisionManager());
    assertNotNull(C2monServiceGateway.getTagManager());
  }
  
  /**
   * Starts the C2MON client API and registers to some tags
   * @param args
   */
  public static void main(String[] args) {
    // Put here your list of tags that you want to test!
    Set<Long> tagIds = new HashSet<Long>();
    tagIds.add(159195L);
    tagIds.add(159135L);
    tagIds.add(156974L);
    tagIds.add(187252L);
    tagIds.add(187248L);
    tagIds.add(187208L);
    tagIds.add(187244L);
    tagIds.add(187227L);
    tagIds.add(165354L);
    tagIds.add(159207L);
    
    try {
      C2monServiceGateway.startC2monClient();
      try {
        // Sleep to give the JmsProxy time to connect (Should be removed soon!)
        Thread.sleep(2000);
      }
      catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      C2monTagManager tagManager = C2monServiceGateway.getTagManager();
      tagManager.subscribeDataTags(tagIds, new DataTagUpdateListener() {
        @Override
        public void onUpdate(ClientDataTagValue tagUpdate) {
          System.out.println("Update received for tag " + tagUpdate.getId() + ":");
          System.out.println("\ttag name           : " + tagUpdate.getName());
          System.out.println("\tvalue              : " + tagUpdate.getValue());
          System.out.println("\ttype               : " + tagUpdate.getTypeNumeric());
          System.out.println("\tvalue description  : " + tagUpdate.getDescription());
          System.out.println("\tquality Code       : " + tagUpdate.getDataTagQuality().toString());
          System.out.println("\tquality description: " + tagUpdate.getDataTagQuality().getDescription());
          System.out.println();
        }
      });
    }
    catch (Exception e) {
      LOG.error("Catched runtime exception on main thread.", e);
      System.exit(1);
    }
  }
}
