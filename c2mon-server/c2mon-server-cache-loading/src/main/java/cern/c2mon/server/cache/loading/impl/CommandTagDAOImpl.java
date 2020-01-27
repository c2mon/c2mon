/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.cache.loading.impl;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.loading.CacheLoaderName;
import cern.c2mon.server.cache.loading.CommandTagDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.common.command.CommandTag;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

/**
 * CommandTag DAO implementation.
 *
 * @author Mark Brightwell
 */
@Service(CacheLoaderName.Names.COMMANDTAG)
public class CommandTagDAOImpl extends AbstractDefaultLoaderDAO<CommandTag> implements CommandTagDAO {

  /**
   * Reference to mapper.
   */
  private CommandTagMapper commandTagMapper;

  @Inject
  public CommandTagDAOImpl(CommandTagMapper commandTagMapper) {
    super(2000, commandTagMapper);
    this.commandTagMapper = commandTagMapper;
  }

  @Override
  public void insert(CommandTag commandTag) {
    commandTagMapper.insertCommandTag((CommandTagCacheObject) commandTag);
  }

  @Override
  public void updateConfig(CommandTag commandTag) {
    commandTagMapper.updateCommandTag((CommandTagCacheObject) commandTag);
  }

  @Override
  public void deleteItem(Long commandTagId) {
    commandTagMapper.deleteCommandTag(commandTagId);
  }

  @Override
  protected CommandTag doPostDbLoading(CommandTag item) {
    //do nothing for this cache
    return item;
  }

}
