package cern.c2mon.client.core;

import java.util.Collection;

import cern.c2mon.client.common.tag.ClientCommandTag;

public interface C2monCommandManager {
  /**
   * Creates a Collection of ClientCommandTags from a Collection of identifiers
   * 
   * @param pIds Collection of unique tag identifiers to create
   *        ClientCommandTags from
   * @return Collection of clientCommandTag instances
   **/
  public Collection<ClientCommandTag> getCommandTags(final Collection<Long> pIds);
}
