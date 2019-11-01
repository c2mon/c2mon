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

import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import java.sql.Timestamp;

public class DataTagCacheObjectFactory extends AbstractTagCacheObjectFactory<DataTagCacheObject> {

  @Override
  public DataTagCacheObject sampleBase() {
    //construct fake DataTagCacheObject, setting all fields
    DataTagCacheObject cacheObject = new DataTagCacheObject();
    cacheObject.setId(100000L);  //must be non null in DB
    cacheObject.setName("Junit_test_datatag1"); //non null
    cacheObject.setDescription("test description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Boolean"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(Boolean.TRUE);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setEquipmentId(100L); //need test equipment inserted
    cacheObject.setMinValue(23.3f);
    cacheObject.setMaxValue(12.2f);
    cacheObject.setAddress(new DataTagAddress());
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setDaqTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setSourceTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("130");
    return cacheObject;
  }

  public DataTagCacheObject sample2() {
    DataTagCacheObject cacheObject = sampleBase();
    cacheObject.setId(100001L);  //must be non null in DB
    cacheObject.setName("Junit_test_datatag2"); //non null
    return cacheObject;
  }

  public DataTagCacheObject sampleDown() {
    DataTagCacheObject cacheObject = sampleBase();
    cacheObject.setId(100003L);  //must be non null in DB
    cacheObject.setName("Junit_test_datatag3"); //non null
    cacheObject.setDataType("String"); // non null
    cacheObject.setUnit("test unit");
    cacheObject.setValue("DOWN");
    cacheObject.getAlarmIds().add(1L);
    cacheObject.getAlarmIds().add(3L);
    return cacheObject;
  }
}
