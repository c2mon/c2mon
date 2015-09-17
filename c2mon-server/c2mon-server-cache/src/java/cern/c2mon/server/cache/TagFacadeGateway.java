package cern.c2mon.server.cache;

import java.util.Collection;

import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.tag.Tag;

/**
 * CommonTagFacade implementation that can be used on any {@link Tag}
 * cache object (DataTag, ControlTag and RuleTag). It locates the correct
 * facade bean to call according to the object passed. 
 * @author Mark Brightwell
 *
 */
public interface TagFacadeGateway extends CommonTagFacade<Tag> {

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
   * Determines whether one of the tag caches already contains
   * an element with the specified id (looks in rule, control
   * and tag cache in that order).
   * 
   * @param id the id to search for
   * @return true if the id corresponds to some tag
   * @see TagLocationService#isInTagCache(Long)
   */
  @Override
  public boolean isInTagCache(Long id);
}
