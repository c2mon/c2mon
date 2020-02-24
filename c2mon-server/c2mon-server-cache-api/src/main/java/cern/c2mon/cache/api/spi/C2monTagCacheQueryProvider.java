package cern.c2mon.cache.api.spi;

import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;

import java.util.Collection;
import java.util.List;

public interface C2monTagCacheQueryProvider {

  /**
   * Return the Tag with associated evaluated Alarms corresponding
   * to the Tag value. A frozen copy is returned.
   *
   * @param id the Tag id
   * @return Tag and Alarms, with corresponding values (no longer residing in cache)
   */
  TagWithAlarms getTagWithAlarms(Long id);

  /**
   * Return a list of Tags where the tag name is matching the given regular expression,
   * with associated evaluated Alarms corresponding to the Tag value.
   * A frozen copy is returned.
   *
   * @param regex Either a Tag name or a regular expression with wildcards ('*' and '?' are supported)
   * @return A list of Tags and Alarms, with corresponding values (no longer residing in cache)
   */
  Collection<TagWithAlarms> getTagsWithAlarms(String regex);

  /**
   * Given a tag, get it's alarms.
   *
   * @param tag The tag.
   * @return A list of alarms
   */
  List<Alarm> getAlarms(Tag tag);

  /**
   * A {@link Tag} can also be retrieved with its unique name
   * that has to correspond to {@link Tag#getName()}. Please
   * note that the query is case insensitive.
   * @param name The unique name of a tag
   * @return The corresponding cache object or <code>null</code>, if
   *         the cache does not contain any tag with this name
   * @see #get(Object)
   * @see #findByNameWildcard(String)
   * @see Tag#getName()
   */
  Tag get(String name);

  /**
   * Determines whether one of the tag caches already contains
   * an element with the specified tag name (looks in rule, control
   * and tag cache in that order).
   *
   * @param name the tag name to search for
   * @return true if the tag name corresponds to some tag
   */
  Boolean isInTagCache(String name);

  /**
   * Searches for all {@link Tag} instances, where
   * the {@link Tag#getName()} attribute matches the given regular
   * Expression.
   * <p>
   * A regular expression matcher. '?' and '*' may be used.
   * The search is always case insensitive.
   * <p>
   * WARN: Expressions starting with a leading wildcard character are
   * potentially very expensive (ie. full scan) for indexed caches
   *
   * @param regex The regular expression including '?' and '*'
   * @return All tags where the tag name is matching the regular expression.
   * Please note, that the result is limited to 100'000 in order to avoid a
   * OutOfMemory exception!
   * @see #get(String)
   */
  Collection<Tag> findByNameWildcard(String regex);
}
