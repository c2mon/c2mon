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
package cern.c2mon.server.cache.test.factory;

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import java.sql.Timestamp;

public class DataTagCacheObjectFactory extends AbstractTagCacheObjectFactory<AbstractInfoTagCacheObject> {

  @Override
  public DataTagCacheObject sampleBase() {
    //construct fake DataTagCacheObject, setting all fields
    return createSample(100000L);
  }

  public DataTagCacheObject sample2() {
    DataTagCacheObject cacheObject = createSample(100001L);
    cacheObject.setName("Junit_test_datatag2"); //non null
    return cacheObject;
  }

  public DataTagCacheObject sampleDown() {
    DataTagCacheObject cacheObject = createSample(100003L);
    cacheObject.setName("Junit_test_datatag3"); //non null
    cacheObject.setDataType("String"); // non null
    cacheObject.setUnit("test unit");
    cacheObject.setValue("DOWN");
    cacheObject.getAlarmIds().add(1L);
    cacheObject.getAlarmIds().add(3L);
    return cacheObject;
  }

  private DataTagCacheObject createSample(long id) {
    DataTagCacheObject base = new DataTagCacheObject(id, "Junit_test_datatag1", "Boolean", DataTagConstants.MODE_TEST);
    base.setDescription("test description");
    base.setLogged(false); //null allowed
    base.setUnit("test unit m/sec");
    base.setDipAddress("testDIPaddress");
    base.setJapcAddress("testJAPCaddress");
    base.setValue(Boolean.TRUE);
    base.setValueDescription("test value description");
    base.setSimulated(false); //null allowed
    base.setEquipmentId(100L); //need test equipment inserted
    base.setMinValue(23.3f);
    base.setMaxValue(12.2f);
    base.setAddress(new DataTagAddress());
    base.setDataTagQuality(createValidQuality());
    base.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    base.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    base.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    base.setRuleIdsString("130");
    return base;
  }
}
