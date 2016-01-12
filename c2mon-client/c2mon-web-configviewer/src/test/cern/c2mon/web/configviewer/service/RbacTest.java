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
package cern.c2mon.web.configviewer.service;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RbacTest {

  
  @Test
  public void testConfigLoaderService() {
    
    String configloader_details = "TIM_APPLICATIONS,TIM_WEBCONFIG,RUN ";
    
    String[] split_config_details = configloader_details.replace(" ", "").split( ",\\s*" ); // split on commas
    
    assertTrue (split_config_details[0].equals("TIM_APPLICATIONS"));
    assertTrue (split_config_details[1].equals("TIM_WEBCONFIG"));
    assertTrue (split_config_details[2].equals("RUN"));
  }
}
