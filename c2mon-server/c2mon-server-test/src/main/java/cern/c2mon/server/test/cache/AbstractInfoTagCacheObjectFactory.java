package cern.c2mon.server.test.cache;

import cern.c2mon.server.common.tag.AbstractInfoTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.common.datatag.DataTagConstants;

import java.sql.Timestamp;

abstract class AbstractInfoTagCacheObjectFactory<T extends AbstractInfoTagCacheObject> extends AbstractTagCacheObjectFactory<T> {

  protected void initDefaults(T base){
    base.setId(100000L);  //must be non null in DB
    base.setName("Junit_test_datatag1"); //non null
    base.setDescription("test description");
    base.setMode(DataTagConstants.MODE_TEST); //non null
    base.setDataType("Boolean"); // non null
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
  }
}
