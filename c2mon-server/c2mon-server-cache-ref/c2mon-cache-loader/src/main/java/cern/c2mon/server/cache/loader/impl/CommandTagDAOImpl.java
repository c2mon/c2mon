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
package cern.c2mon.server.cache.loader.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.CommandTagMapper;
import cern.c2mon.server.cache.loader.CommandTagDAO;
import cern.c2mon.server.cache.loader.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.common.command.CommandTag;

/**
 * CommandTag DAO implementation.
 *
 * @author Mark Brightwell
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
