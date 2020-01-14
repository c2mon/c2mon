package cern.c2mon.server.common.tag;

import cern.c2mon.shared.common.datatag.DataTagAddress;

import java.sql.Timestamp;

/**
 * Interface giving access to the most important attributes of a DataTag.
 *
 * It only provides read methods as in general this object should only be modified
 * by the cache modules (with the object residing in the cache).
 */
public interface InfoTag extends Tag {

  /**
   * Returns the timestamp of the value set at source.
   * @return the Timestamp set at the equipment level
   */
  Timestamp getSourceTimestamp();

  /**
   * The DAQ timestamp indicates when the value change message has been sent from the DAQ.
   * @return The DAQ timestamp
   */
  Timestamp getDaqTimestamp();

  DataTagAddress getAddress();

}
