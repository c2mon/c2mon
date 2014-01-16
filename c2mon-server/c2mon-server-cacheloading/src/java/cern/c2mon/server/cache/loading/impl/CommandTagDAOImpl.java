package cern.c2mon.server.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * CommandTag DAO implementation.
 * 
 * @author Mark Brightwell
 *
 */
@Service("commandTagDAO")
public class CommandTagDAOImpl extends AbstractDefaultLoaderDAO<CommandTag> implements CommandTagDAO {

  /**
   * Reference to mapper.
   */
  private CommandTagMapper commandTagMapper;
    
  @Autowired
  public CommandTagDAOImpl(CommandTagMapper commandTagMapper) {
    super(2000, commandTagMapper);
    this.commandTagMapper = commandTagMapper;
  }

  @Override
  public void insertCommandTag(CommandTag commandTag) {
    commandTagMapper.insertCommandTag((CommandTagCacheObject) commandTag);
  }

  @Override
  public void updateCommandTag(CommandTag commandTag) {
    commandTagMapper.updateCommandTag((CommandTagCacheObject) commandTag);
  }
  
  @Override
  public void deleteCommandTag(Long commandTagId) {
    commandTagMapper.deleteCommandTag(commandTagId);
  }

  @Override
  protected CommandTag doPostDbLoading(CommandTag item) {
    //do nothing for this cache
    return item;
  }

}
