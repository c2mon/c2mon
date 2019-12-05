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
package cern.c2mon.server.configuration.handler.impl;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.configuration.handler.TagConfigHandler;
import cern.c2mon.server.configuration.handler.transacted.TagConfigTransacted;

/**
 * Public methods in this class should perform the complete
 * configuration process for the given tag (i.e. cache update
 * and database persistence).
 * 
 * <p>The methods contain the common reconfiguration logic for
 * all Tag objects (Control, Data and Rule tags).
 * 
 * <p>The appropriate Facade and DAO objects must be passed
 * to the constructor to provide the common configuration
 * functionality. 
 * 
 * <p>Notice that these methods will always be called within
 * a transaction initiated at the ConfigurationLoader level
 * and passed through the handler via a "create", "update"
 * or "remove" method, with rollback of DB changes if a 
 * RuntimeException is thrown.
 * 
 * @author Mark Brightwell
 * 
 * @param <T> the type of Tag
 *
 */
class TagConfigHandlerImpl<T extends Tag> implements TagConfigHandler<T> {
  
  /**
   * Ref to transacted bean.
   */
  private TagConfigTransacted tagConfigTransacted;
  
  /**
   * Constructor.
   * @param tagConfigTransacted bean with transacted methods
   */
  public TagConfigHandlerImpl(TagConfigTransacted tagConfigTransacted) {
    super();
    this.tagConfigTransacted = tagConfigTransacted;
  }

  @Override
  public void addAlarmToTag(Long tagId, Long alarmId) {
    tagConfigTransacted.addAlarmToTag(tagId, alarmId);
  }

  @Override
  public void addRuleToTag(Long tagId, Long ruleId) {
    tagConfigTransacted.addRuleToTag(tagId, ruleId);
  }

  @Override
  public void removeAlarmFromTag(Long tagId, Long alarmId) {
    tagConfigTransacted.removeAlarmFromTag(tagId, alarmId);
  }

  @Override
  public void removeRuleFromTag(Long tagId, Long ruleId) {
    tagConfigTransacted.removeRuleFromTag(tagId, ruleId);
  }
  
  
}
