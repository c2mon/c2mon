package cern.c2mon.client.core.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import cern.c2mon.client.common.listener.DataTagUpdateListener;
import cern.c2mon.client.common.tag.ClientDataTag;
import cern.c2mon.client.core.manager.HistoryManager;

/**
 * This interface provides basic access methods to the client cache and
 * should be used by all core classes who needs to get directly access
 * to the cached <code>ClientDataTag</code> references.
 *
 * @author Matthias Braeger
 */
public interface BasicCacheHandler {
  /**
   * Returns a reference to the <code>CLientDataTag</code> object in the
   * cache.
   * @param tagId The tag id
   * @return The <code>ClientDataTag</code> reference or <code>null</code>,
   *         if the cache does not contain the specific tag.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  ClientDataTag get(Long tagId);
  
  /**
   * Returns a reference map to to those <code>ClientDataTag</code> objects in the
   * cache which have been specified with the entry set. If the the cache is missing
   * an entry the returning map will contain a <code>null</code> pointer value for that
   * id.  
   * @param tagIds list of tag ids.
   * @return Map of <code>ClientDataTag</code> references.
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Map<Long, ClientDataTag> get(Set<Long> tagIds);
  
  /**
   * Returns all <code>ClientDataTag</code> references in the cache which have
   * the given <code>DataTagUpdateListener</code> registered.
   * @param listener The listener for which all registered tags shall be returned
   * @return A collection of <code>ClientDataTag</code> references
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Collection<ClientDataTag> getAllTagsForListener(DataTagUpdateListener listener);
  
  /**
   * Returns all <code>ClientDataTag</code> references in the cache which are linked
   * to the given equipment id.
   * @param equipmentId The equipment id
   * @return A collection of <code>ClientDataTag</code> references
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Collection<ClientDataTag> getAllTagsForEquipment(Long equipmentId);
  
  /**
   * Returns all <code>ClientDataTag</code> references in the cache which are linked
   * to the given DAQ process id.
   * @param processId The process id
   * @return A collection of <code>ClientDataTag</code> references
   * @throws NullPointerException When the parameter is <code>null</code>
   */
  Collection<ClientDataTag> getAllTagsForProcess(Long processId);
  
  
  /**
   * @return A reference list to all <code>ClientDataTag</code> objects
   *         which have at least one <code>DataTagUpdateListener</code>
   *         listener subscribed.
   */
  Collection<ClientDataTag> getAllSubscribedDataTags();
  
  
  /**
   * Enables or disables the History mode of the cache. In history mode all
   * getter-methods will then return references to objects in the history cache.
   * Also the registered <code>DataTagUpdateListener</code>'s will then receive
   * updates from the history cache.
   * <p>
   * However, the internal live cache is still update will live events and stays
   * up to date once it is decided to switch back into live mode.
   * <p>
   * Please note that this method can be locked by other threads. Locking is
   * realized with the {@link #getHistoryModeSyncLock()} method.
   * <p>
   * This method shall only be used by the {@link HistoryManager}
   *  
   * @param enable <code>true</code>, for enabling the history mode
   * @see #getHistoryModeSyncLock()
   */
  void setHistoryMode(boolean enable);
  
  /**
   * @return <code>true</code>, if the history mode of the cache is enabled 
   */
  boolean isHistoryModeEnabled();
  
  /** 
   * The returning object can be used for preventing any thread changing the 
   * the cache mode. The {@link #setHistoryMode(boolean)} method is internally
   * synchronizing on the same object.
   * @return The synchronization object for locking other thread changing the
   *         cache mode.
   * @see #setHistoryMode(boolean)
   */
  Object getHistoryModeSyncLock();
  
  /**
   * Checks whether the tag with the given tag id is already cached.
   * @param tagId tag id
   * @return <code>true</code>, if the tag with the given tag id is in the cache.
   * @throws NullPointerException If the specified key is <code>null</code>
   */
  boolean containsTag(Long tagId);
}
