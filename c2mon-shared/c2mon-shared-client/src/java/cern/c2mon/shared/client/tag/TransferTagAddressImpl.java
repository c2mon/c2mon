package cern.c2mon.shared.client.tag;

/**
 * Object used for tranferring DataTagAddress information
 * to the Client API.
 * 
 * @author Mark Brightwell
 *
 */
public class TransferTagAddressImpl implements TransferTagAddress {

  /**
   * @see DataTagAddress
   */
  private int timeToLive;
  
  /**
   * @see DataTagAddress
   */
  private short valueDeadbandType;
  
  /**
   * @see DataTagAddress
   */
  private float valueDeadband;
  
  /**
   * @see DataTagAddress
   */
  private int timeDeadband;
  
  /**
   * @see DataTagAddress
   */
  private int priority;
  
  /**
   * @see DataTagAddress
   */
  private boolean guaranteedDelivery;
  
  /**
   * Hardware address is transferred as XML String.
   */
  private String hardwareAddress;

  /**
   * Constructor.
   */
  public TransferTagAddressImpl(int timeToLive, short valueDeadbandType, float valueDeadband, int timeDeadband,
      int priority, boolean guaranteedDelivery, String hardwareAddress) {
    super();
    this.timeToLive = timeToLive;
    this.valueDeadbandType = valueDeadbandType;
    this.valueDeadband = valueDeadband;
    this.timeDeadband = timeDeadband;
    this.priority = priority;
    this.guaranteedDelivery = guaranteedDelivery;
    this.hardwareAddress = hardwareAddress;
  }
  
  /**
   * For Json.
   */
  private TransferTagAddressImpl() {    
  }

  /**
   * @return the timeToLive
   */
  @Override
  public int getTimeToLive() {
    return timeToLive;
  }

  /**
   * @return the valueDeadbandType
   */
  @Override
  public short getValueDeadbandType() {
    return valueDeadbandType;
  }

  /**
   * @return the valueDeadband
   */
  @Override
  public float getValueDeadband() {
    return valueDeadband;
  }

  /**
   * @return the timeDeadband
   */
  @Override
  public int getTimeDeadband() {
    return timeDeadband;
  }

  /**
   * @return the priority
   */
  @Override
  public int getPriority() {
    return priority;
  }

  /**
   * @return the guaranteedDelivery
   */
  @Override
  public boolean isGuaranteedDelivery() {
    return guaranteedDelivery;
  }

  /**
   * @return the hardwareAddress
   */
  @Override
  public String getHardwareAddress() {
    return hardwareAddress;
  }
  
  
}
