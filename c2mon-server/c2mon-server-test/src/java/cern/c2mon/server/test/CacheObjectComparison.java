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
package cern.c2mon.server.test;

import static org.junit.Assert.assertEquals;
import cern.c2mon.server.common.alarm.AlarmCacheObject;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;

/**
 * Contains static methods for comparing cache objects in tests.
 * 
 * @author Mark Brightwell
 *
 */
public final class CacheObjectComparison {

  /**
   * Hide constructor.
   */
  private CacheObjectComparison() { };
  
  /**
   * Compares CommandTags.
   * @param commandTag1 first tag
   * @param commandTag2 second tag
   */
  public static void equals(final CommandTagCacheObject commandTag1, final CommandTagCacheObject commandTag2) {
    assertEquals(commandTag1.getId(), commandTag2.getId());
    assertEquals(commandTag1.getName(), commandTag2.getName());
    assertEquals(commandTag1.getDescription(), commandTag2.getDescription());
    assertEquals(commandTag1.getDataType(), commandTag2.getDataType());
    assertEquals(commandTag1.getMode(), commandTag2.getMode());
    assertEquals(commandTag1.getEquipmentId(), commandTag2.getEquipmentId());
    assertEquals(commandTag1.getHardwareAddress().toConfigXML(), commandTag2.getHardwareAddress().toConfigXML());
    assertEquals(commandTag1.getSourceTimeout(), commandTag2.getSourceTimeout());
    assertEquals(commandTag1.getSourceRetries(), commandTag2.getSourceRetries());
    assertEquals(commandTag1.getExecTimeout(), commandTag2.getExecTimeout());
    assertEquals(commandTag1.getClientTimeout(), commandTag2.getClientTimeout());    
    assertEquals(commandTag1.getAuthorizationDetails(), commandTag2.getAuthorizationDetails());
    assertEquals(commandTag1.getMinimum(), commandTag2.getMinimum());
    assertEquals(commandTag1.getMaximum(), commandTag2.getMaximum());
    
    assertEquals(commandTag1.getProcessId(), commandTag2.getProcessId());
  }
  
  /**
   * Compares DataTags.
   * @param dataTag1 first tag
   * @param dataTag2 second tag
   */
  public static void equals(final DataTagCacheObject dataTag1, final DataTagCacheObject dataTag2) {
    equalsTag(dataTag1, dataTag2);
    assertEquals(dataTag1.getEquipmentId(), dataTag2.getEquipmentId());    
    assertEquals(dataTag1.getAddress().toConfigXML(), dataTag2.getAddress().toConfigXML());
    assertEquals(dataTag1.getCacheTimestamp(), dataTag2.getCacheTimestamp());
    assertEquals(dataTag1.getDaqTimestamp(), dataTag2.getDaqTimestamp());       
    assertEquals(dataTag1.getStatus(), dataTag2.getStatus());   
    assertEquals(dataTag1.getProcessId(), dataTag2.getProcessId());
    assertEquals(dataTag1.isLogged(), dataTag2.isLogged());        
  }
  
  /**
   * Compares two Tags.
   * @param tag1 first tag
   * @param tag2 second tag
   */
  public static void equalsTag(final Tag tag1, final Tag tag2) {
    assertEquals(tag1.getId(), tag2.getId());
    assertEquals(tag1.getName(), tag2.getName());
    assertEquals(tag1.getDescription(), tag2.getDescription());
    assertEquals(tag1.getDataType(), tag2.getDataType());
    assertEquals(tag1.getMode(), tag2.getMode());
    assertEquals(tag1.getAlarmIds(), tag2.getAlarmIds());
    assertEquals(tag1.getRuleIds(), tag2.getRuleIds());
    assertEquals(tag1.getTimestamp(), tag2.getTimestamp());
    assertEquals(tag1.getDataTagQuality(), tag2.getDataTagQuality());   
    assertEquals(tag1.getDipAddress(), tag2.getDipAddress());
    assertEquals(tag1.getJapcAddress(), tag2.getJapcAddress());
    assertEquals(tag1.getValueDictionary().toXML(), tag2.getValueDictionary().toXML());
    assertEquals(tag1.getValue(), tag2.getValue());
    assertEquals(tag1.getUnit(), tag2.getUnit());
    assertEquals(tag1.getValueDescription(), tag2.getValueDescription()); 
    assertEquals(tag1.isExistingTag(), tag2.isExistingTag()); 
  }
  
  /**
   * Assert all Alarm fields are equal.
   * @param alarm1 first alarm
   * @param alarm2 second alarm
   */
  public static void equals(final AlarmCacheObject alarm1, final AlarmCacheObject alarm2) {
    assertEquals(alarm1.getId(), alarm2.getId());
    assertEquals(alarm1.getTagId(), alarm2.getTagId());
    assertEquals(alarm1.getFaultCode(), alarm2.getFaultCode());
    assertEquals(alarm1.getFaultFamily(), alarm2.getFaultFamily());
    assertEquals(alarm1.getFaultMember(), alarm2.getFaultMember());
    assertEquals(alarm1.getState(), alarm2.getState());
    assertEquals(alarm1.getInfo(), alarm2.getInfo());
    assertEquals(alarm1.getCondition(), alarm2.getCondition());
    assertEquals(alarm1.getTimestamp(), alarm2.getTimestamp());
    assertEquals(alarm1.isPublished(), alarm2.isPublished());
    if (alarm1.getLastPublication() != null) {
      assertEquals(alarm1.getLastPublication().getState(), alarm2.getLastPublication().getState());
      assertEquals(alarm1.getLastPublication().getInfo(), alarm2.getLastPublication().getInfo());
      assertEquals(alarm1.getLastPublication().getPublicationTime(), alarm2.getLastPublication().getPublicationTime());
    }    
  }
  
  
  
}
