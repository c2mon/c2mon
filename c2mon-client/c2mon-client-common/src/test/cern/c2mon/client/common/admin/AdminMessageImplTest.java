/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.common.admin;

import static org.junit.Assert.*;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.common.admin.AdminMessage.AdminMessageType;

/**
 * Test for the {@link AdminMessageImpl} class
 * 
 * @author vdeila
 *
 */
public class AdminMessageImplTest {

  /** The object that the test will be executed on */
  private AdminMessageImpl adminMessage;
  
  @Before
  public void setUp() throws Exception {
    adminMessage = new AdminMessageImpl(AdminMessageType.INFO, "Test Sender", "The message", new Timestamp(System.currentTimeMillis()));
  }

  @Test
  public void testToJson() {
    final String json = adminMessage.toJson();
    assertNotNull(adminMessage);
    assertEquals(adminMessage, AdminMessageImpl.fromJson(json));
  }
  
  @Test
  public void testClone() throws CloneNotSupportedException {
    assertEquals(adminMessage, adminMessage.clone());
  }
  
  @Test
  public void testCopyConstructor() {
    assertEquals(adminMessage, new AdminMessageImpl(adminMessage));
  }
  
}
