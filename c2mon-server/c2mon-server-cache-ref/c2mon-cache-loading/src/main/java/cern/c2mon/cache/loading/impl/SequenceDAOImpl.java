/*
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
 */

package cern.c2mon.cache.loading.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.cache.loading.SequenceDAO;
import cern.c2mon.server.cache.dbaccess.SequenceMapper;

/**
 * @author Franz Ritter
 */
@Service
public class SequenceDAOImpl implements SequenceDAO {

  private SequenceMapper sequenceMapper;

  @Autowired
  public SequenceDAOImpl(SequenceMapper sequenceMapper) {
    this.sequenceMapper = sequenceMapper;
  }


  @Override
  public Long getNextConfigId() {
    return sequenceMapper.getNextConfigId();
  }

  @Override
  public Long getNextProcessId() {
    return sequenceMapper.getNextProcessId();
  }

  @Override
  public Long getNextEquipmentId() {
    return sequenceMapper.getNextEquipmentId();
  }

  @Override
  public Long getNextTagId() {
    return sequenceMapper.getNextTagId();
  }

  @Override
  public Long getNextAlarmId() {
    return sequenceMapper.getNextAlarmId();
  }
}
