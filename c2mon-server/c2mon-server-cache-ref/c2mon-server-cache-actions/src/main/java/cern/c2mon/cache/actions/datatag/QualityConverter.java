package cern.c2mon.cache.actions.datatag;

import cern.c2mon.shared.common.datatag.DataTagQuality;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.SourceDataTagQuality;

import static cern.c2mon.shared.common.datatag.TagQualityStatus.*;

/**
 * Provides methods for calculating the quality of a DataTag
 * cache object based on the quality of the incoming source tag.
 * @author mbrightw
 *
 */
public class QualityConverter {

  private QualityConverter() {

  }

  /**
   * Converts the {@link SourceDataTagQuality} into the correct {@link DataTagQuality}.
   * Not this method does not update the DataTag object, but returns a new {@link DataTagQuality} object. 
   * @param sourceDataTagQuality the incoming value quality
   * @return the new quality for the server object
   */
  public static DataTagQuality convert(SourceDataTagQuality sourceDataTagQuality){
    DataTagQuality newTagQuality;

    if (sourceDataTagQuality == null) {
      newTagQuality = new DataTagQualityImpl();
      newTagQuality.validate();
      return newTagQuality;
    }

    switch(sourceDataTagQuality.getQualityCode()) {
      case OK:
        newTagQuality = new DataTagQualityImpl();
        newTagQuality.validate();
        break;
      case OUT_OF_BOUNDS:
        newTagQuality = new DataTagQualityImpl(VALUE_OUT_OF_BOUNDS, sourceDataTagQuality.getDescription());
        break;
      case DATA_UNAVAILABLE:
        newTagQuality = new DataTagQualityImpl(INACCESSIBLE, sourceDataTagQuality.getDescription());
        break;
      default:
        newTagQuality = new DataTagQualityImpl(UNKNOWN_REASON, sourceDataTagQuality.getDescription());
        break;
    }

    return newTagQuality;
  }

}