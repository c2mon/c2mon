package cern.c2mon.web.configviewer.util;

/**
 * Helper Class for the TrendViewer.
 * 
 * Includes simple information about an invalidation point (Time and Reason for the invalidation).
 * Passed to the .jsp TrendView. 
 * 
 * @see https://issues.cern.ch/browse/TIMS-873
 * 
 * @author ekoufaki
 */
public class InvalidPoint {

  /** Time of invalidation */
  private final String time;
  
  /** Reason for the invalidation */
  private final String invalidationReason;
  
  public InvalidPoint(final String time, final String invalidationReason) {
    this.time = time;
    this.invalidationReason = invalidationReason;
  }
  
  public String getTime() {
    return time;
  }
  
  public String getInvalidationReason() {
    return invalidationReason;
  }
}
