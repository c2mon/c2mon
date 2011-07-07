package cern.c2mon.client.core.tag;

import java.util.Collection;

import cern.c2mon.client.core.DataTagUpdateListener;
import cern.c2mon.shared.client.tag.TransferTag;
import cern.c2mon.shared.client.tag.TransferTagValue;
import cern.tim.shared.common.datatag.TagQualityStatus;
import cern.tim.shared.rule.RuleFormatException;

/**
 * This interface extends the <code>ClientDataTagValue</code> interface 
 * and provides all methods which are necessary to update a
 * <code>ClientDataTag</code> object. This interface  shall only
 * be used within the C2MON client API. In other words no classes outside
 * of the C2MON client API should make directly changes on a
 * <code>ClientDataTag</code> object.
 *
 * @author Matthias Braeger
 */
public interface ClientDataTag extends ClientDataTagValue {

  /**
   * Invalidates the tag with {@link TagQualityStatus#INACCESSIBLE} and sets
   * the quality description to <code>pDescription</code>
   * Notifies all registered <code>DataTagUpdateListeners</code> of the change
   * of state.
   * @param pDescription the quality description
   */
  void invalidate(final String pDescription);

  /**
   * Adds a <code>DataTagUpdateListener</code> to the ClientDataTag and 
   * generates an initial update event for that listener.
   * Any change to the ClientDataTag value or quality attributes will trigger
   * an update event to all <code>DataTagUpdateListener</code> objects 
   * registered.
   * @param pListener the DataTagUpdateListener comments
   * @see #removeUpdateListener(DataTagUpdateListener)
   */
  void addUpdateListener(final DataTagUpdateListener pListener);

  /**
   * 
   * @return All listeners registered to this data tag
   */
  Collection<DataTagUpdateListener> getUpdateListeners();

  /**
   * Returns <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   * @param pListener the listener to check
   * @return <code>true</code>, if the given listener is registered
   * for receiving updates of that tag.
   */
  boolean isUpdateListenerRegistered(DataTagUpdateListener pListener);

  /**
   * Removes (synchronized) a previously registered <code>DataTagUpdateListener</code>
   * @see #addUpdateListener
   * @param pListener The listener that shall be unregistered
   */
  void removeUpdateListener(final DataTagUpdateListener pListener);

  /**
   * Returns information whether the tag has any update listeners registered
   * or not
   * @return <code>true</code>, if this <code>ClientDataTag</code> instance has
   *         update listeners registered.
   */
  boolean hasUpdateListeners();

  /**
   * This thread safe method updates the given <code>TransferTagValue</code> object.
   * It copies every single field of the <code>TransferTag</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * @param tag The object that contains the updates.
   */
  boolean update(final TransferTagValue transferTagValue);

  
  /**
   * This thread safe method updates the given <code>ClientDataTag</code> object.
   * It copies every single field of the <code>TransferTag</code> object and notifies
   * then the registered listener about the update by providing a copy of the
   * <code>ClientDataTag</code> object.
   * @param tag The object that contains the updates.
   */
  boolean update(final TransferTag transferTag) throws RuleFormatException;

  /**
   * @return A <code>String</code> representation of the JMS destination where the DataTag 
   *         is published on change.
   */
  String getTopicName();
  
  /**
   * Creates a clone of the this object. The only difference is that
   * it does not copy the registered listeners.
   * @return The clone of this object
   * @throws CloneNotSupportedException Thrown, if one of the field does not support cloning.
   */
  ClientDataTag clone() throws CloneNotSupportedException;
}