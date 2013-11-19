package cern.c2mon.shared.common.datatag.address;


/**
 * The LASERHardwareAddress interface is used by the LASERMessageHandler. 
 * It contains all information necessary to extract a data element from any 
 * LASER publication. 
 * @see cern.c2mon.daq.laser.LASERMessageHandler
 * @author W. Buczak
 * @version $Revision: 1.0
 */
 public interface LASERHardwareAddress extends HardwareAddress {
 
  // ---------------------------------------------------------------------------
  // Public accessor methods
  // ---------------------------------------------------------------------------
  
  /** 
   * Get the name of the LASER alarm category
   * The category me can never be null.
   * @return the name of the alarm category
   */
  public String getAlarmCategory();
  
 /** 
   * Get the name of the alarm family
   * The alarm family can never be null.  
   * @return the name of the alarm category
   */
  public String getFaultFamily();
  
 /** 
   * Get the name of the alarm fault member
   * The fault member me can never be null.
   * @return the name of the alarm category
   */  
  public String getFaultMember();
  
 /** 
   * Get the name of the alarm fault code
   * The code me can never be null.
   * @return the name of the alarm category
   */  
  public int getFalutCode();

}
