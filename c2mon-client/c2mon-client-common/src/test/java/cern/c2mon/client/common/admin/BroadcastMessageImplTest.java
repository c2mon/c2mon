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
package cern.c2mon.client.common.admin;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.admin.BroadcastMessage.BroadcastMessageType;

/**
 * Test for the {@link BroadcastMessageImpl} class
 * 
 * @author vdeila
 *
 */
public class BroadcastMessageImplTest {

  /** The object that the test will be executed on */
  private BroadcastMessageImpl adminMessage;
  
  @Before
  public void setUp() throws Exception {
    adminMessage = new BroadcastMessageImpl(BroadcastMessageType.INFO, "Test Sender", "The message", new Timestamp(System.currentTimeMillis()));
  }

  @Test
  public void testToJson() {
    final String json = adminMessage.toJson();
    assertNotNull(adminMessage);
    assertEquals(adminMessage, BroadcastMessageImpl.fromJson(json));
  }
  
  @Test
  public void testClone() throws CloneNotSupportedException {
    assertEquals(adminMessage, adminMessage.clone());
  }
  
  @Test
  public void testCopyConstructor() {
    assertEquals(adminMessage, new BroadcastMessageImpl(adminMessage));
  }
  
}
