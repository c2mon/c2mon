package cern.c2mon.cache.impl.query;

import cern.c2mon.cache.api.C2monCache;
import cern.c2mon.cache.api.spi.C2monTagCacheQueryProvider;
import cern.c2mon.cache.impl.IgniteC2monCache;
import cern.c2mon.server.common.alarm.Alarm;
import cern.c2mon.server.common.alarm.TagWithAlarms;
import cern.c2mon.server.common.alarm.TagWithAlarmsImpl;
import cern.c2mon.server.common.tag.Tag;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class IgniteC2monTagCacheQueryProvider implements C2monTagCacheQueryProvider {

  private final IgniteC2monCache<Alarm> alarmCache;
  private final IgniteC2monCache<Tag> tagCache;

  @Inject
  public IgniteC2monTagCacheQueryProvider(final C2monCache<Alarm> alarmCache, final C2monCache<Tag> tagCache) {
    this.alarmCache = (IgniteC2monCache<Alarm>) alarmCache;
    this.tagCache = (IgniteC2monCache<Tag>) tagCache;
  }

  @Override
  public TagWithAlarms getTagWithAlarms(Long id) {
//    TODO Check if this should be done as a getCopy() call? The original version was using that,
//    and I can see why, but they also expected that all cache changes are done from one thread,
//     hence the alarms access did not need validation
    Tag tag = tagCache.get(id);

    return tag != null ? new TagWithAlarmsImpl(tag, getAlarms(tag)) : null;
  }

  @Override
  public Collection<TagWithAlarms> getTagsWithAlarms(String regex) {
    return findByNameWildcard(regex).stream().map(tag -> getTagWithAlarms(tag.getId())).collect(Collectors.toList());
  }

  @Override
  public List<Alarm> getAlarms(Tag tag) {
//    TODO Check what happens if tag id is modified on the fly?
    return new ArrayList<>(alarmCache.query(alarm -> alarm.getDataTagId().equals(tag.getId())));
  }

  @Override
  public Tag get(String name) {
    return tagCache.query(tag -> tag.getName().equals(name)).stream().findFirst().orElse(null);
  }

  @Override
  public Boolean isInTagCache(String name) {
    return tagCache.query(tag -> tag.getName().equals(name)).isEmpty();
  }

  @Override
  public Collection<Tag> findByNameWildcard(String regex) {
    return tagCache.query(tag -> tag.getName().matches(regex));
  }
}
