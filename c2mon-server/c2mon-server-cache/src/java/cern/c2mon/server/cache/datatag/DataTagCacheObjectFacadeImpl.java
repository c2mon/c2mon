package cern.c2mon.server.cache.datatag;

import java.sql.Timestamp;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.tag.AbstractTagObjectFacade;
import cern.c2mon.server.common.datatag.DataTag;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.AbstractTagCacheObject;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.DataTagQuality;

@Service
public class DataTagCacheObjectFacadeImpl extends AbstractTagObjectFacade<DataTag> implements DataTagCacheObjectFacade {
  
  /**
   * Class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(DataTagCacheObjectFacadeImpl.class);
  
  /**
   * Update the DataTag object held in the cache with the provided values. 
   * Notice that the datatag quality is not validated and needs to be done
   * independently using the validate public method.
   */
  @Override
  public void update(final DataTag dataTag, final Object value, final String valueDesc, final Timestamp sourceTimestamp, final Timestamp daqTimestamp, final Timestamp cacheTimestamp) {           
    updateValue(dataTag, value, valueDesc);    
    setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp);                  
  }
  
  @Override
  public void updateAndInvalidate(DataTag dataTag, Object value, String valueDescription, Timestamp sourceTimestamp, Timestamp daqTimestamp,
                                      Timestamp cacheTimestamp, DataTagQuality dataTagQuality) {    
    updateValue(dataTag, value, valueDescription);
    setTimestamps(dataTag, sourceTimestamp, daqTimestamp, cacheTimestamp);
    ((AbstractTagCacheObject) dataTag).setDataTagQuality(dataTagQuality);    
  } 
  
  /**
   * Should always be used for modifying the timestamps.
   * @param dataTag
   * @param sourceTimestamp
   * @param srvTimestamp
   */
  @Override
  public void setTimestamps(DataTag dataTag, Timestamp sourceTimestamp, Timestamp daqTimestamp, Timestamp cacheTimestamp) {    
    // TODO change this to modifying the timestamps if they are not null, rather than putting a new object
    DataTagCacheObject dataTagCacheObject = (DataTagCacheObject) dataTag;    
    dataTagCacheObject.setSourceTimestamp(sourceTimestamp);
    dataTagCacheObject.setDaqTimestamp(daqTimestamp);
    dataTagCacheObject.setCacheTimestamp(cacheTimestamp);    
  }
  
  @Override
  public void setQuality(final DataTag dataTag, final DataTagQuality dataTagQuality, final Timestamp cacheTimestamp) {
    ((DataTagCacheObject) dataTag).setDataTagQuality(dataTagQuality);
    ((DataTagCacheObject) dataTag).setCacheTimestamp(cacheTimestamp);    
  }

 /**
   * Creates an empty DataTagCacheObject with the provided id and sets all the
   * fields assumed none-null to default values.
   */
  public DataTagCacheObject createEmptyDataTag(Long id) {
    DataTagCacheObject dataTagCacheObject = new DataTagCacheObject();
    dataTagCacheObject.setId(id);
    dataTagCacheObject.setName("UNKNOWN");
    dataTagCacheObject.setMode(DataTagConstants.MODE_TEST); //default is TEST mode
    dataTagCacheObject.setLogged(false); //default not logged
    dataTagCacheObject.setSimulated(false); //default is not simulated
    return dataTagCacheObject;
  }
  
}
