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
package cern.c2mon.server.cache.datatag;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;

/**
 * Unit test of DataTagCacheObjectFacadeImpl
 * 
 * @author Mark Brightwell
 *
 */
public class DataTagCacheObjectFacadeImplTest {

  /**
   * To test
   */
  private DataTagCacheObjectFacadeImpl facade;
  
  @Before
  public void setUp() {
    facade = new DataTagCacheObjectFacadeImpl();
  }
  
  @Test
  public void testValueDescriptionTruncation() {    
    DataTagCacheObject dataTag = new DataTagCacheObject(Long.valueOf(2), "test name", "Float", DataTagConstants.MODE_OPERATIONAL);
    
    //provided description from source
    char[] chars = new char[DataTagCacheObjectFacade.MAX_DESC_LENGTH + 1];
    Arrays.fill(chars, 'v');
    chars[DataTagCacheObjectFacade.MAX_DESC_LENGTH] = 'a';
    String valueDesc = new String(chars);
    
    //expected description in tag
    char[] charsTag = new char[DataTagCacheObjectFacade.MAX_DESC_LENGTH];
    Arrays.fill(charsTag, 'v');  
    String takenDesc = new String(charsTag);
    
    facade.updateValue(dataTag, 1f, valueDesc);
    
    assertEquals(dataTag.getValueDescription(), takenDesc);
    
    
  }
  
}
