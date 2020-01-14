/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.control.ControlTagCacheObject;

public class ControlTagCacheObjectFactory extends AbstractTagCacheObjectFactory<ControlTagCacheObject> {

  @Override
  public ControlTagCacheObject sampleBase() {
    ControlTagCacheObject cacheObject = new ControlTagCacheObject(1001L);

    cacheObject.setName("Junit_test_tag"); //non null
    cacheObject.setDataType("Float"); // non null
    cacheObject.setValue(1000f);
    cacheObject.setEquipmentId(150L); //need test equipment inserted
    cacheObject.setMinValue(100f);
    cacheObject.setMaxValue(2000f);
    cacheObject.setRuleIdsString(""); //same as setting to null
    return cacheObject;
  }
}
