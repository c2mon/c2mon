package cern.c2mon.client.common.tag;

/**
 * This interface represents the read-only interface
 * of the command tag, as it is cached on the C2MON
 * Client API.
 * 
 * @deprecated Got replaced by {@link CommandTag}
 * @author Matthias Braeger
 */
@Deprecated
public interface ClientCommandTag<T> extends CommandTag<T>{
  // Leave empty!
}
