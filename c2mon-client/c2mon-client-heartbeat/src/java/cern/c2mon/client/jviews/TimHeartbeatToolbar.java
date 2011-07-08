package cern.c2mon.client.jviews;

/**
 * Class kept for backward compatibility reasons
 * @author J. Stowisek
 * @Deprecated Please use class ch.cern.tim.client.jviews.ConnectionStateToolbar instead
 */

 public class TimHeartbeatToolbar extends ConnectionStateToolbar {
  /**
     * Serial Version UID for the TimHeartbeatToolbar class
     */
    private static final long serialVersionUID = -5025784917853367882L;

  /**
   * Constructor kept for backward compatibility reasons
   * @Deprecated Please use class ch.cern.tim.client.jviews.ConnectionStateToolbar instead
   */
  public TimHeartbeatToolbar() {
  }

  public static void main(String[] args) {
    new TimHeartbeatToolbar();
  }
}