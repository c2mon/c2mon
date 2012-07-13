/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

/** Interface which describes a listener for the TagCache 
 * 
 * @author ${user} 
 * @version $Revision$, $Date$, $Author$
 */
public interface TagCacheUpdateListener {
    
    /**
     * Called whenever a new update on the TagCache took place.
     * @param incoming a Tag object representing the latest value.
     */
    public void onUpdate(Tag incoming);

}
