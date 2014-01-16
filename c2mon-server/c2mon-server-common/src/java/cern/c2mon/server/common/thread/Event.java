package cern.c2mon.server.common.thread;

/**
 * Used for internal server synchronization.
 * Old events should be rejected by beans, that keep
 * track of the time of the latest event they processed.
 * 
 * @author Mark Brightwell
 *
 * @param <T> the return type of the event
 */
public class Event<T> {

  /**
   * The time of the event, as provided by the processing bean.
   */
  private long eventTime;
  
  /**
   * The return value of the event, if any (o.w. is null).
   */
  private T returnValue;
  
  /**
   * When no return value is passed.
   * @param eventTime time of the event
   */
  public Event(long eventTime) {
    super();
    this.eventTime = eventTime;
    validate();
  }
  
  /**
   * Constructor when return value is needed
   * @param eventTime time of the event
   * @param returnValue return value
   */
  public Event(long eventTime, T returnValue) {
    super();
    this.eventTime = eventTime;
    this.returnValue = returnValue;
    validate();
  }

  private void validate() {
    //no retrictions so far
  }

  /**
   * @return the eventTime
   */
  public long getEventTime() {
    return eventTime;
  }

  /**
   * @return the returnValue
   */
  public T getReturnValue() {
    return returnValue;
  }
 
  
  
}
