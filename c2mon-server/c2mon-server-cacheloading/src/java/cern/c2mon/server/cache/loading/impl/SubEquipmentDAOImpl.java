/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2011 CERN.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.cache.loading.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.dbaccess.SubEquipmentMapper;
import cern.c2mon.server.cache.loading.SubEquipmentDAO;
import cern.c2mon.server.cache.loading.common.AbstractDefaultLoaderDAO;
import cern.c2mon.server.common.exception.SubEquipmentException;
import cern.c2mon.server.common.subequipment.SubEquipment;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;

/**
 * SubEquipment DAO implementation.
 * @author Mark Brightwell
 *
 */
@Service("subEquipmentDAO")
public class SubEquipmentDAOImpl extends AbstractDefaultLoaderDAO<SubEquipment> implements SubEquipmentDAO {

  /**
   * Private logger.
   */
  private Logger LOGGER = Logger.getLogger(SubEquipmentDAOImpl.class);

  /**
   * Reference to iBatis mapper.
   */
  private SubEquipmentMapper subEquipmentMapper;
  
  @Autowired
  public SubEquipmentDAOImpl(SubEquipmentMapper subEquipmentMapper) {
    super(500, subEquipmentMapper);
    this.subEquipmentMapper = subEquipmentMapper;
  }
  
  /**
   * Creates a new subEquipment entity in the database
   * 
   * @param subEquipment
   *          The information representing the subEquipment to be stored
   * @throws SubEquipmentException
   *           A SubEquipmentException is thrown in case the entity could not be
   *           stored
   */
  @Override
  public void create(final SubEquipmentCacheObject subEquipment) throws SubEquipmentException {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("create() - Create a subEquipment with the id: " + subEquipment.getId());
    }
    try {      
      subEquipmentMapper.insertSubEquipment(subEquipment);
    } catch (DataAccessException e) {
      //TODO add these catch clauses to all the DAO classes...
      throw new SubEquipmentException(e.getMessage());
    }
  }

  /**
   * Retrieves the subEquipment entity specified by its id
   * 
   * @param subEquipmentId
   *          The id of the subEquipment to be retrieved
   * @return A SubEquipmentCacheObject representing a row of the equipment table
   * @throws SubEquipmentException
   *           An exception is thrown if the execution of the query failed
   */
  @Override
  public SubEquipment getSubEquipmentById(final Long subEquipmentId) throws SubEquipmentException {
    SubEquipment eq = null;
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getSubEquipmentById() - Getting the subEquipment with id: " + subEquipmentId);
    }
    try {
      eq = (SubEquipment) getItem(subEquipmentId);      
    } catch (DataAccessException e) {
      throw new SubEquipmentException(e.getMessage());
    }
    return eq;
  }

  /**
   * Retrieves all the subEquipments attached to the indicated equipment
   * 
   * @param equipmentId
   *          The id of the equipment for which we want to retrieve its
   *          subEquipments
   * @return List of SubEquipmentCacheObject attached to the indicated equipment
   * @throws SubEquipmentException
   *           An exception is thrown if the execution of the query failed
   */
  @Override
  public List<SubEquipment> getSubEquipmentsByEquipment(final Long equipmentId) throws SubEquipmentException {
    List<SubEquipment> subEquipments = null;
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("getSubEquipmentsByEquipment() - Retrieving the subEquipments attached to the equipment with id " + equipmentId);
    }
    try {
      subEquipments = subEquipmentMapper.selectSubEquipmentsByEquipment(equipmentId);      
    } catch (DataAccessException e) {
      throw new SubEquipmentException(e.getMessage());
    }
    return subEquipments;
  }

  @Override
  public void deleteItem(final Long id) {    
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("remove() - Removing the SubEquipment with id " + id);
    }
    subEquipmentMapper.deleteSubEquipment(id);         
  }

  @Override
  public void insert(SubEquipment cacheable) {
    subEquipmentMapper.insertSubEquipment((SubEquipmentCacheObject) cacheable);
  }

  @Override
  public void updateConfig(SubEquipment cacheable) {
    subEquipmentMapper.updateSubEquipmentConfig((SubEquipmentCacheObject) cacheable);
  }

  @Override
  protected SubEquipment doPostDbLoading(SubEquipment item) {
    //do nothing for this cache
    return item;
  }



}
