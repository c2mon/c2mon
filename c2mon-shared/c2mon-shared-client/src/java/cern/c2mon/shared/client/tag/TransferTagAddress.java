package cern.c2mon.shared.client.tag;

/**
 * Interface to transfer object used for Tag
 * address info.
 * 
 * @author Mark Brightwell
 *
 */
public interface TransferTagAddress {

  /**
   * @see DataTagAddress
   */
  boolean isGuaranteedDelivery();

  /**
   * @see DataTagAddress
   */
  int getPriority();

  /**
   * @see DataTagAddress
   */
  int getTimeDeadband();

  /**
   * @see DataTagAddress
   */
  float getValueDeadband();

  /**
   * @see DataTagAddress
   */
  short getValueDeadbandType();

  /**
   * @see DataTagAddress
   */
  int getTimeToLive();

  /**
   * @see DataTagAddress
   */
  String getHardwareAddress();

  
}
