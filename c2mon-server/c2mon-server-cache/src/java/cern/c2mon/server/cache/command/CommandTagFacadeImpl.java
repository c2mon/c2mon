/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2005-2010 CERN.
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
package cern.c2mon.server.cache.command;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.server.cache.CommandTagCache;
import cern.c2mon.server.cache.CommandTagFacade;
import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.common.AbstractFacade;
import cern.c2mon.server.cache.exception.CacheElementNotFoundException;
import cern.c2mon.server.common.command.CommandTagCacheObject;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.ConfigurationException;
import cern.c2mon.shared.common.SimpleTypeReflectionHandler;
import cern.c2mon.shared.common.datatag.DataTagConstants;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.common.datatag.address.HardwareAddressFactory;
import cern.c2mon.shared.common.type.TagDataType;
import cern.c2mon.shared.daq.command.CommandTag;
import cern.c2mon.shared.daq.command.SourceCommandTag;
import cern.c2mon.shared.daq.config.CommandTagUpdate;
import cern.c2mon.shared.daq.config.HardwareAddressUpdate;


/**
 * Implementation of the CommandTagFacade.
 */
@Service
public class CommandTagFacadeImpl extends AbstractFacade<CommandTag> implements CommandTagFacade {
  
  /**
   * Private class logger.
   */
  private static final Logger LOGGER = Logger.getLogger(CommandTagFacadeImpl.class);
  
  /**
   * Reference to the cache.
   */
  private CommandTagCache commandTagCache;
  
  /**
   * Equipment cache.
   */
  private EquipmentCache equipmentCache;
  
  @Autowired
  public CommandTagFacadeImpl(CommandTagCache commandTagCache, EquipmentCache equipmentCache) {
    super();
    this.commandTagCache = commandTagCache;
    this.equipmentCache = equipmentCache;
  }

  /**
   * Generates the XML needed to send to the DAQ at start-up.
   * @param id
   * @return
   */
  @Override
  public String getConfigXML(Long id) {
    String returnValue = "";
    try {
      CommandTag commandTag = commandTagCache.getCopy(id);
      returnValue = generateSourceXML(commandTag); //old version: SourceDataTag.toConfigXML(tag);     
    } catch (CacheElementNotFoundException cacheEx) {
      LOGGER.error("Failed to locate command tag with id " + id + " in the cache (returning empty String config).");
    }    
    return returnValue;    
  }

  @Override
  public CommandTag createCacheObject(Long id, Properties properties) throws IllegalAccessException {
    CommandTag commandTag = new CommandTagCacheObject(id);        
    configureCacheObject(commandTag, properties);
    validateConfig(commandTag);
    //invalidateQuietly(dataTag, new DataTagQuality(DataTagQuality.UNINITIALISED, "DataTag created"), new Timestamp(System.currentTimeMillis()));
    return commandTag; 
  }
  
  /**
   * Also called from ControlTagFacade.
   */
  @Override
  public SourceCommandTag generateSourceCommandTag(CommandTag commandTag) {
    SourceCommandTag sourceCommandTag = new SourceCommandTag(commandTag.getId(), 
                                                             commandTag.getName(), 
                                                             commandTag.getSourceTimeout(), 
                                                             commandTag.getSourceRetries(), 
                                                             commandTag.getHardwareAddress());    
    return sourceCommandTag;
  }
  
  @Override
  public CommandTagUpdate configureCacheObject(CommandTag commandTag, Properties properties) {
    String tmpStr = null;
    CommandTagCacheObject commandTagCacheObject = (CommandTagCacheObject) commandTag;
    CommandTagUpdate commandTagUpdate = new CommandTagUpdate();
    commandTagUpdate.setCommandTagId(commandTag.getId());
    // id (Long) TODO should be able to remove this as id is always set beforehand and never changed in update
//    if ((tmpStr = properties.getProperty("id")) != null) {
//      try {
//        commandTagCacheObject.setId(valueOf(tmpStr));
//      }
//      catch (NumberFormatException e) {
//        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"id\" to Long: " + tmpStr);
//      }
//    }
    // name (String)
    if ((tmpStr = properties.getProperty("name")) != null) {
      commandTagCacheObject.setName(tmpStr);
      commandTagUpdate.setName(tmpStr);
    }    
    // description (String)
    tmpStr = properties.getProperty("description"); 
    if (tmpStr != null) {
      commandTagCacheObject.setDescription(tmpStr);     
    }    
    // mode (short)
    if ((tmpStr = properties.getProperty("mode")) != null) {
      try {
        commandTagCacheObject.setMode(Short.parseShort(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"mode\" to short: " + tmpStr);
      }
    }
    // dataType (String)
    if (properties.getProperty("dataType") != null) {
      commandTagCacheObject.setDataType(properties.getProperty("dataType"));
    }    
    
    // sourceRetries (int)
    if ((tmpStr = properties.getProperty("sourceRetries")) != null) {
      try {
        commandTagCacheObject.setSourceRetries(Integer.parseInt(tmpStr));
        commandTagUpdate.setSourceRetries(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"sourceRetries\" to int: " + tmpStr);
      }
    }
    // sourceTimeout (int)
    if ((tmpStr = properties.getProperty("sourceTimeout")) != null) {
      try {
        commandTagCacheObject.setSourceTimeout(Integer.parseInt(tmpStr));
        commandTagUpdate.setSourceTimeout(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"sourceTimeout\" to int: " + tmpStr);
      }
    }
    // execTimeout (int)
    if ((tmpStr = properties.getProperty("execTimeout")) != null) {
      try {
        commandTagCacheObject.setExecTimeout(Integer.parseInt(tmpStr)); 
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"execTimeout\" to int: " + tmpStr);
      }
    }
    // clientTimeout (int)
    if ((tmpStr = properties.getProperty("clientTimeout")) != null) {
      try {
        commandTagCacheObject.setClientTimeout(Integer.parseInt(tmpStr));
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"clientTimeout\" to int: " + tmpStr);
      }
    }
    // hardwareAddress (String -> HardwareAddress)
    if ((tmpStr = properties.getProperty("hardwareAddress")) != null) {
      try {
        HardwareAddress hardwareAddress = HardwareAddressFactory.getInstance().fromConfigXML(tmpStr);
        commandTagCacheObject.setHardwareAddress(hardwareAddress);
        setUpdateHardwareAddress(hardwareAddress, commandTagUpdate);
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Exception: Unable to create HardwareAddress from parameter \"hardwareAddress\": " + tmpStr);
      }
    }

    // minValue (Comparable)
    if ((tmpStr = properties.getProperty("minValue"))!= null) {
      try {
        if (commandTagCacheObject.getDataType().equals("Float")) {
          commandTagCacheObject.setMinimum(Float.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Integer")) {
          commandTagCacheObject.setMinimum(Integer.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Double")) {
          commandTagCacheObject.setMinimum(Double.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Long")) {
          commandTagCacheObject.setMinimum(Long.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Short")) {
          commandTagCacheObject.setMinimum(Short.valueOf(tmpStr));
        }
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            "NumberFormatException: Unable to convert parameter \"minValue\" to " + commandTagCacheObject.getDataType() + ": " + tmpStr);
      }
    }
    // Try to extract maxValue of the appropriate data type
    if ((tmpStr = properties.getProperty("maxValue"))!= null) {
      try {
        if (commandTagCacheObject.getDataType().equals("Float")) {
          commandTagCacheObject.setMaximum(Float.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Integer")) {
          commandTagCacheObject.setMaximum(Integer.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Double")) {
          commandTagCacheObject.setMaximum(Double.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Long")) {
          commandTagCacheObject.setMaximum(Long.valueOf(tmpStr));
        }
        else if (commandTagCacheObject.getDataType().equals("Short")) {
          commandTagCacheObject.setMaximum(Short.valueOf(tmpStr));
        }
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, 
            "NumberFormatException: Unable to convert parameter \"maxValue\" to " + commandTagCacheObject.getDataType() + ": " + tmpStr);
      }
    }

    RbacAuthorizationDetails authorizationDetails = new RbacAuthorizationDetails();
    boolean configureAuthorization = false;
    if ((tmpStr = properties.getProperty("rbacClass")) != null) {
      authorizationDetails.setRbacClass(tmpStr);
      configureAuthorization = true;
    } else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacClass(commandTagCacheObject.getAuthorizationDetails().getRbacClass());
    }
    if ((tmpStr = properties.getProperty("rbacDevice")) != null) {
      authorizationDetails.setRbacDevice(tmpStr);
      configureAuthorization = true;
    } else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacDevice(commandTagCacheObject.getAuthorizationDetails().getRbacDevice());
    }
    if ((tmpStr = properties.getProperty("rbacProperty")) != null) {
      authorizationDetails.setRbacProperty(tmpStr);
      configureAuthorization = true;
    } else if (commandTagCacheObject.getAuthorizationDetails() != null) {
      authorizationDetails.setRbacProperty(commandTagCacheObject.getAuthorizationDetails().getRbacProperty());
    }
    if (configureAuthorization) {
      commandTagCacheObject.setAuthorizationDetails(authorizationDetails);   
    }
    
    // equipmentId (Long) - not currently used, as need to remove and add for this change
    if ((tmpStr = properties.getProperty("equipmentId")) != null) {
      try {
        commandTagCacheObject.setEquipmentId(Long.valueOf(tmpStr));
        commandTagCacheObject.setProcessId(equipmentCache.get(commandTagCacheObject.getEquipmentId()).getProcessId());
      }
      catch (NumberFormatException e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "NumberFormatException: Unable to convert parameter \"equipmentId\" to Long: " + tmpStr);
      }
    }
    return commandTagUpdate;
  }

  /**
   * Sets the {@link HardwareAddress} field in the {@link CommandTagUpdate}.
   * @param hardwareAddress the new {@link HardwareAddress}
   * @param commandTagUpdate the update object that will be sent to the DAQ
   * @throws IllegalAccessException 
   * @throws  
   */
  private void setUpdateHardwareAddress(HardwareAddress hardwareAddress, CommandTagUpdate commandTagUpdate) throws IllegalAccessException {
    HardwareAddressUpdate hardwareAddressUpdate = new HardwareAddressUpdate(hardwareAddress.getClass().getName());
    commandTagUpdate.setHardwareAddressUpdate(hardwareAddressUpdate);
    SimpleTypeReflectionHandler reflectionHandler = new SimpleTypeReflectionHandler();
    for (Field field : reflectionHandler.getNonTransientSimpleFields(hardwareAddress.getClass())) {
      field.setAccessible(true);
      hardwareAddressUpdate.getChangedValues().put(field.getName(), field.get(hardwareAddress));
    }
  }

  /**
   * Checks all fields of a Command Tag satisfy requirements.
   * @param commandTag
   * @throws ConfigurationException
   */
  public void validateConfig(CommandTag commandTag) throws ConfigurationException {
    if (commandTag.getId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"id\" cannot be null");
    }
    if (commandTag.getName() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" cannot be null");
    }
    if (commandTag.getName().length() == 0 || commandTag.getName().length() > 60) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"name\" must be 1 to 60 characters long");
    }
    if (commandTag.getDescription() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" cannot be null");
    }
    if (commandTag.getDescription().length() > 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"description\" can be up to 100 characters long");
    }
    switch (commandTag.getMode()) {
      case DataTagConstants.MODE_OPERATIONAL : break;
      case DataTagConstants.MODE_TEST : break;
      case DataTagConstants.MODE_MAINTENANCE : break;
      default :
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Invalid value for parameter \"mode\" : " + commandTag.getMode());
    }
    
    if (commandTag.getDataType() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" cannot be null");
    }
    if (!TagDataType.isValidDataType(commandTag.getDataType())) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"dataType\" can only be Boolean, Integer, Float, Double, Long or String");
    }
    
    if (commandTag.getSourceRetries() < 0 || commandTag.getSourceRetries() > 3) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"sourceRetries\" must be between 0 and 3");
    }
    
    if (commandTag.getSourceTimeout() < 100) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"sourceTimeout\" must be >= 100");
    }
    
    if (commandTag.getClientTimeout() < 5000) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"clientTimeout\" must be >= 5000");
    }
    
    if (commandTag.getClientTimeout() < commandTag.getExecTimeout()) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"clientTimeout\" must be greater than execTimeout");
    }
    
    if (commandTag.getExecTimeout() < (commandTag.getSourceTimeout() * (commandTag.getSourceRetries() + 1))) {
      LOGGER.debug("sourceTimeout: " + commandTag.getSourceTimeout() + " sourceRetries: " + commandTag.getSourceRetries() + " execTimeout: " + commandTag.getExecTimeout());
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"execTimeout\" must be greater than (sourceRetries + 1) * sourceTimeout");
    }
    
    if (commandTag.getMinimum() != null) {
      try {
        Class minValueClass = Class.forName("java.lang." + commandTag.getDataType());
        if (!minValueClass.isInstance(commandTag.getMinimum())) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"mininum\" must be of type " + commandTag.getDataType() + " or null");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Error validating parameter \"minimum\": " + e.getMessage());
      }
      
    }
    
    if (commandTag.getMaximum() != null ) {
      try {
        Class maxValueClass = Class.forName("java.lang." + commandTag.getDataType());
        if (!maxValueClass.isInstance(commandTag.getMaximum())) {
          throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"maximum\" must be of type " + commandTag.getDataType() + " or null");
        }
      }
      catch (Exception e) {
        throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Error validating parameter \"maximum\": " + e.getMessage());
      }
      
    }
    
    if (commandTag.getEquipmentId() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Parameter \"equipmentId\" cannot be null");
    }
    if (commandTag.getAuthorizationDetails() == null) {
      throw new ConfigurationException(ConfigurationException.INVALID_PARAMETER_VALUE, "Authorization details cannot be null.");
    }
  }
  
  /**
   * Used to be implemented in SourceCommandTag object (TODO still be be removed there)
   * @param cmd
   * @return
   */
  private String generateSourceXML(CommandTag cmd) {
    StringBuffer str = new StringBuffer("    <CommandTag id=\"");

    str.append(cmd.getId());
    str.append("\" name=\"");
    str.append(cmd.getName());
    str.append("\">\n");

    str.append("      <source-timeout>");
    str.append(cmd.getSourceTimeout());
    str.append("</source-timeout>\n");

    str.append("      <source-retries>");
    str.append(cmd.getSourceRetries());
    str.append("</source-retries>\n");

    if (cmd.getHardwareAddress() != null) {
      str.append(cmd.getHardwareAddress().toConfigXML());
    }

    str.append("    </CommandTag>\n");
    return str.toString();   
  }
  
  
}
