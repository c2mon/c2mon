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

import cern.c2mon.server.common.rule.RuleTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import java.sql.Timestamp;

public class RuleTagCacheObjectFactory extends AbstractTagCacheObjectFactory<RuleTagCacheObject> {

  @Override
  public RuleTagCacheObject sampleBase() {
    RuleTagCacheObject cacheObject =
      new RuleTagCacheObject(130L,
        "Junit_test_tag",
        "Integer",
        DataTagConstants.MODE_TEST,
        "(#100000 = true)&(#100001 = true)[2],true[3]"); //rule text set here - only extra field on top of abstract class
    cacheObject.setName("Junit_test_rule_tag"); //non null
    cacheObject.setDescription("test rule description");
    cacheObject.setMode(DataTagConstants.MODE_TEST); //non null
    cacheObject.setDataType("Integer"); // non null
    cacheObject.setLogged(false); //null allowed
    cacheObject.setUnit("test unit m/sec");
    cacheObject.setDipAddress("testDIPaddress");
    cacheObject.setJapcAddress("testJAPCaddress");
    cacheObject.setValue(1000);
    cacheObject.setValueDescription("test value description");
    cacheObject.setSimulated(false); //null allowed
    cacheObject.setDataTagQuality(createValidQuality());
    cacheObject.setCacheTimestamp(new Timestamp(System.currentTimeMillis()));
    cacheObject.setRuleIdsString("");
    return cacheObject;
  }
}
