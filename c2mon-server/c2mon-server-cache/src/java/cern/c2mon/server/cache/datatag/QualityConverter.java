package cern.c2mon.server.cache.datatag;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.daq.datatag.SourceDataQuality;

/**
 * Provides methods for calculating the quality of a DataTag
 * cache object based on the quality of the incoming source tag.
 * @author mbrightw
 *
 */
public interface QualityConverter {

  /**
   * Converts the {@link SourceDataQuality} into the correct {@link DataTagQuality}.
   * Not this method does not update the DataTag object, but returns a new {@link DataTagQuality} object. 
   * @param sourceDataQuality the incoming value quality
   * @return the new quality for the server object
   */
  DataTagQuality convert(SourceDataQuality sourceDataQuality);

}
