package cern.c2mon.util.buffer;

import java.util.Collection;
import java.util.EventObject;

/** A buffer pull event class. A PullEvent instance
 * is passed as parameter to the SynchroBufferListener pull method.
 * @author F.Calderini
 */
public class PullEvent extends EventObject {
    private Collection pulled;
    
    /** Creates a new instance of PullEvent.
     * @param source the event source
     * @param pulled the pulled objects
     */
    public PullEvent(Object source, Collection pulled) {
        super(source);
        this.pulled = pulled;
    }
    
    /** Accessor method. 
     * @return the pulled objects
     */
    public Collection getPulled() {
        return pulled;
    }

    public String toString() 
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("[source=");
      buffer.append(getSource());
      buffer.append(", pulled=");
      buffer.append(getPulled());
      buffer.append("]");

      return buffer.toString();
    }
    
}

