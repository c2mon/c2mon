package cern.c2mon.client.history.playback.publish;

import java.util.Collection;

import cern.c2mon.client.common.listener.TagUpdateListener;

/**
 * This class keeps lists of which listeners is listening on which tag.
 * 
 * @author vdeila
 *
 */
public class TagListenersManager extends KeyForValuesMap<Long, TagUpdateListener> {

  /**
   * Constructor
   */
  public TagListenersManager() {
    super();
  }

  /**
   * 
   * @param tagId
   *          The tag id the listener wants to listen to
   * @param listener
   *          The listener to add
   * @return <code>true</code> if this is the first listener registered on the
   *         tag
   */
  @Override
  public synchronized boolean add(final Long tagId, final TagUpdateListener listener) {
    return super.add(tagId, listener);
  }

  /**
   * Removes all listeners
   */
  @Override
  public void clear() {
    super.clear();
  }

  /**
   * 
   * @param tagId
   *          the tag id to get the listeners for
   * @return the listeners registered on the <code>tagId</code>
   */
  @Override
  public synchronized Collection<TagUpdateListener> getValues(final Long tagId) {
    return super.getValues(tagId);
  }

  /**
   * 
   * @param tagId
   *          tag id
   * @return <code>true</code> if the tag id have listeners
   */
  @Override
  public synchronized boolean haveKey(final Long tagId) {
    return super.haveKey(tagId);
  }

  /**
   * 
   * @param listener
   *          The listener to remove
   * @return a list of tags which doesn't have any listeners after the removal
   *         of this listener
   */
  @Override
  public synchronized Collection<Long> remove(final TagUpdateListener listener) {
    return super.remove(listener);
  }
  
  
  
}
