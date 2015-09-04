/*******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2004 - 2011 CERN. This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 ******************************************************************************/
package cern.c2mon.client.core;

import java.util.Collection;
import java.util.Set;

import cern.c2mon.client.common.listener.BaseTagListener;
import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.cache.CacheSynchronizationException;

/**
 * This interface describes the methods which are provided by
 * the C2MON TagService singleton. 
 * <p>
 * The tag service allows e.g. subscribing listeners to tags
 * to get informed when a new update is received.
 *
 * @author Matthias Braeger
 */
public interface TagService {

  /**
   * Use this method for registering a listener and to receive the current (initial) values and updates
   * for the list of specified data tags.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   *
   * @param tagIds A collection of data tag IDs
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tags. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(Set, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribe(final Set<Long> tagIds, final BaseTagListener listener) throws CacheSynchronizationException;

  /**
   * Registers a listener to receive the current (initial) value and updates for one specific data tag.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   *
   * @param tagId The unique identifier of the data tag you want to subscribe to
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(Long, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribe(final Long tagId, final BaseTagListener listener) throws CacheSynchronizationException;
  
  /**
   * Registers a listener to receive the current (initial) values and updates for all tags where the
   * name matches the regular expression. If the string contains no special characters the server will
   * only return the tag whose name is equals the string.
   * <p>
   * <b>Please note</b>, that the call is NOT constantly checking in the background whether new tags have been
   * configured on the server that would match the given list!
   * <p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before.
   * <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   * <p>
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param regex A concrete tag name or wildcard expression, which shall be used to subscribe to all matching data tags.
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(String, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribeByName(final String regex, final BaseTagListener listener) throws CacheSynchronizationException;

  /**
   * Registers a listener to receive the current (initial) values and updates for all tags where the
   * name matches the regular expression. If the string contains no special characters the server will
   * only return the tag whose name is equals the string.
   * <p>
   * <b>Please note</b>, that the call is NOT constantly checking in the background whether new tags have been
   * configured on the server that would match the given list!
   * <p>
   * The method will return the initial value(s) of the subscribed tag(s) to {@link TagListener#onInitialUpdate(Collection)}. <b>Please note</b>
   * that the {@link TagListener#onUpdate(Tag)} method will then not receive the initial value.
   * <p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before.
   * <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   * <p>
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param regex A concrete tag name or wildcard expression, which shall be used to subscribe to all matching data tags.
   * @param listener the listener which shall be registered and which will receive the initial values in 
   *                 a separate method
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(String, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribeByName(final String regex, final TagListener listener) throws CacheSynchronizationException;

  
  /**
   * Registers a listener to receive the current (initial) values and updates for all tags, where the
   * name matches the regular expression.
   * <p>
   * <b>Please note</b>, that the call is NOT constantly checking in the background whether new tags have been
   * configured on the server that would match the given list!
   * <p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before.
   * <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   * <p>
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param regexList List of concrete tag names and/or wildcard expressions, which shall be used to subscribe to all matching data tags.
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(String, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribeByName(final Set<String> regexList, final BaseTagListener listener) throws CacheSynchronizationException;

  /**
   * Registers a listener to receive the current (initial) values and updates for all tags, where the
   * name matches the provided string and/or regular expression list.
   * <p>
   * <b>Please note</b>, that the call is NOT constantly checking in the background whether new tags have been
   * configured on the server that would match the given list!
   * <p>
   * The method will return the initial values of the subscribed tags to {@link TagListener#onInitialUpdate(Collection)}. <b>Please note</b>
   * that the {@link TagListener#onUpdate(Tag)} method will then not receive the initial value.
   * <p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before.
   * <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   * <p>
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param regexList List of concrete tag names and/or wildcard expressions, which shall be used to subscribe to all matching data tags.
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(String, TagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  void subscribeByName(final Set<String> regexList, final TagListener listener) throws CacheSynchronizationException;

  
  /**
   * Registers a listener to receive updates for specific data tags.
   * The method will return the initial values of the subscribed tags to {@link TagListener#onInitialUpdate(Collection)}.
   * <b>Please note</b> that the {@link TagListener#onUpdate(Tag)} method will then not
   * receive the initial values.<p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   *
   * @param tagIds A collection of data tag IDs
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tags. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(Set, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribe(final Set<Long> tagIds, final TagListener listener) throws CacheSynchronizationException;

  /**
   * Registers a listener to receive updates for a specific data tag.
   * <p>
   * The method will return the initial value of the subscribed tag to {@link TagListener#onInitialUpdate(Collection)}. <b>Please note</b>
   * that the {@link TagListener#onUpdate(Tag)} method will then not receive the initial value.
   * <p>
   * The C2MON client API will handle for you in the background the initialization of the data
   * tags with the C2MON server, if this was not already done before. <p>
   * Your listener will be informed about new updates via the <code>onUpdate(Tag)</code>
   * method.
   *
   * @param tagId The unique identifier of the data tag you want to subscribe to
   * @param listener the listener which shall be registered
   * @throws CacheSynchronizationException In case a communication problem with the C2MON server
   *         occurs while subscribing to the tag. In that case the {@link TagService} will
   *         rollback the subscription.
   * @see #subscribe(Long, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  void subscribe(final Long tagId, final TagListener listener) throws CacheSynchronizationException;

  
  
  /**
   * Use this method for unregistering a listener from receiving updates for specific data tags.
   *
   * @param tagIds A collection of data tag id's
   * @param listener the listener which shall be registered
   */
  void unsubscribe(final Set<Long> tagIds, final BaseTagListener listener);

  /**
   * Unregisters a listener from receiving updates for specific data tag.
   *
   * @param tagId The unique identifier of the data tag from which we want to unsubscribe
   * @param listener the listener which shall be registered
   */
  void unsubscribe(final Long tagId, final BaseTagListener listener);


  /**
   * Use this method to unsubscribe from all previously registered data tags.
   * @param listener the listener which shall be registered
   */
  void unsubscribe(final BaseTagListener listener);


  /**
   * Returns for a given listener a copy of all subscribed data tags with
   * their current state as <code>Tag</code> instances.
   *
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of <code>Tag</code> objects
   */
  Collection<Tag> getSubscriptions(final BaseTagListener listener);

  /**
   * Returns for a given listener a list of all subscribed data tags ids.
   *
   * @param listener The listener for which we want to get the data tags
   *        subscriptions
   * @return A collection of tag ids
   */
  Set<Long> getSubscriptionIds(final BaseTagListener listener);

  /**
   * Returns for the given id a copy of the cached data tag.
   * If the tag is not in the local cache it will try to fetch it from the server.
   * In case of an unknown tag id the result will be an empty {@link Tag}
   * object.
   * <p>
   * <b>Please notice</b>, that this method call does not write anything to the local
   * cache. This means that you might increase the server load when asking constantly
   * for tags on which no {@link BaseTagListener} is subscribed to.
   *
   * @param tagId A data tag id
   * @return A <code>Tag</code> object
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #get(Collection)
   * @see #subscribe(Set, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking()
   */
  Tag get(final Long tagId);

  /**
   * Returns for every valid id of the list a copy of the cached data tag.
   * If the value is not in the local cache it will try to fetch it from the server.
   * However, in case of an unknown tag id the corresponding tag might be missing.
   * <p>
   * <b>Please notice</b>, that this method call does not write anything to the local
   * cache. This means that you might increase the server load when asking constantly
   * for tags on which no {@link BaseTagListener} is subscribed to.
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>ClientData</code> objects
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #subscribe(Set, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  Collection<Tag> get(final Collection<Long> tagIds);
  
  /**
   * Returns a list of tags which match the given wilcard expression. Different to
   * {@link #get(Collection)} this call will always result in a server request.
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>Tag</code> objects
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #subscribe(Set, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  Collection<Tag> findByName(final String regex);
  
  /**
   * Returns a list of all tags which match the given list of wilcard expressions. Different to
   * {@link #get(Collection)} this call will always result in a server request.
   * <p />
   * Expressions are always case insensitive
   * <p />
   * The following special characters are supported:
   * <ul>
   * <li> '?' - match any one single character </li>
   * <li> '*' - match any multiple character(s) (including zero) </li>
   * </ul>
   * The supported wildcard characters can be escaped with a backslash '\', and a literal backslash can be included with '\\'
   * <p />
   * WARN: Expressions starting with a leading wildcard character are potentially very expensive (ie. full scan) for indexed caches 
   *
   * @param tagIds A collection of data tag id's
   * @return A collection of all <code>Tag</code> objects
   * @throws RuntimeException In case a communication problems with JMS or the C2MON server
   *         occurs while trying to retrieve tag information.
   * @see #subscribe(Set, BaseTagListener)
   * @see C2monSupervisionManager#isServerConnectionWorking();
   */
  Collection<Tag> findByName(final Set<String> regexList);

  /**
   * Returns the total number of subscribed tags in the local cache (cache size).
   * @return the cache size
   */
  int getCacheSize();

  /**
   * This method is used to synchronize subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed data tags. The C2MON client API will then send an update
   * to all subscribed listeners.
   * @throws CacheSynchronizationException In case a communicatin problem with the C2MON server
   *         occurs while refreshing to the tags.
   */
  void refresh() throws CacheSynchronizationException;

  /**
   * This method is used to synchronize a list subscribed data tags with the
   * server. It will ask the server to send the actual tag information for
   * all subscribed tags of the given list. The C2MON client API will then send
   * an update to all subscribed listeners.
   *
   * @param tagIds A collection of data tag id's
   * @throws NullPointerException if the Collection is <code>null</code>.
   * @throws CacheSynchronizationException In case a communicatin problem with the C2MON server
   *         occurs while refreshing to the tags.
   */
  void refresh(Collection<Long> tagIds) throws CacheSynchronizationException;

  /**
   * Checks whether the given listener is subscribed to any data tags.
   *
   * @param listener the listener to check subscriptions for
   * @return true if the listener is subscribed, false otherwise
   */
  boolean isSubscribed(BaseTagListener listener);
}
