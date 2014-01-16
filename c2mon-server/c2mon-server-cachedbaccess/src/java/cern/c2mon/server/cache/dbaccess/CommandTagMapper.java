package cern.c2mon.server.cache.dbaccess;

import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Interface to bean for persisting the CommandTagCacheObject
 * in the cache persistence DB.
 * 
 * @author Mark Brightwell
 *
 */
public interface CommandTagMapper extends LoaderMapper<CommandTag> {

  /**
   * Insert into DB.
   * @param commandTag cache object to persist
   */
  void insertCommandTag(CommandTagCacheObject commandTag);

  /**
   * Remove the command from the DB.
   * @param id of the command
   */
  void deleteCommandTag(Long id);

  /**
   * Updates the command
   * @param modifiedCommand the modified cache object
   */
  void updateCommandTag(CommandTagCacheObject modifiedCommand);

}
