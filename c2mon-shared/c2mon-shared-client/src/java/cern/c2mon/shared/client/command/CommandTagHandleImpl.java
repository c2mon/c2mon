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
package cern.c2mon.shared.client.command;

import java.io.Serializable;

import cern.c2mon.shared.common.command.AuthorizationDetails;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;


/**
 * Implementation of the CommandTagHandle interface.
 * 
 * @param <T> the type of the command value
 *
 * @author Jan Stowisek, Mark Brightwell
 * @version $Revision: 1.5 $ ($Date: 2005/04/29 12:48:10 $ - $State: Exp $)
 */

public class CommandTagHandleImpl<T> implements CommandTagHandle<T>, Serializable {

  /**
   * Serialization id.
   */
  private static final long serialVersionUID = 9211395921321803621L;

  /**
   * Unique numeric identifier of the CommandTag represented by the 
   * present CommandTagHandle object.
   */
  private Long id;

  /**
   * Name of the CommandTag represented by the present CommandTagHandle object.
   */
  private String name;

  /**
   * (Optional) free-text description of the CommandTag represented by 
   * the present CommandTagHandle object.
   */
  private String description;

  /**
   * Name of the data type of the present CommandTagHandle object. Only values 
   * of this data type can be set using setValue().
   */
  private String dataType;

  /**
   * Client timeout in milliseconds.
   * When a client sends a CommandTagHandle to the server for execution and 
   * has not received a CommandTagReport after 'clientTimeout' milliseconds,
   * it should consider the command execution as failed.
   */
  private int clientTimeout;

  /**
   * Authorized minimum for the command value. 
   * If the client tries to set a value less than this minimum, the 
   * setValue() method will throw a CommandTagValueException. If the minValue 
   * is null, it is not taken into account. The minValue will always be null
   * for non-numeric commands.
   */
  private Comparable<T> minValue;

  /**
   * Authorized maximum for the command value. 
   * If the client tries to set a value greater than this maximum, the 
   * setValue() method will throw a CommandTagValueException. If the maxValue 
   * is null, it is not taken into account. The maxValue will always be null
   * for non-numeric commands.
   */
  private Comparable<T> maxValue;

  /**
   * The command's value as set by the user.
   * This field will always be null before the user executes the setValue()
   * method.
   */
  private T value;
  
  /**
   * Details needed to authorise the command on the client.
   */
  private RbacAuthorizationDetails rbacAuthorizationDetails;
  
  /** sourceTimeout */
  private int sourceTimeout;
  
  /** execTimeout */
  private int execTimeout;
  
  /** sourceRetries */
  private int sourceRetries;
  
  /** processId */
  private Long processId;

  /** hardwareAddress */
  private HardwareAddress hardwareAddress;

  /**
   * Public default constructor.
   */
  public CommandTagHandleImpl() {    
  }
  
  /**
   * Constructor
   * This constructor is used on the server side if a user requests a 
   * CommandTagHandle for a CommandTag that does not exist on the server side.
   */
  public CommandTagHandleImpl(final Long pId, final String pHostName) {
    this(pId, CMD_UNKNOWN, CMD_UNKNOWN, CMD_UNKNOWN, 1, null, null,
        pHostName, null);
  }
  
  /**
   * Complete Constructor
   * CommandTagHandle objects must never be created by a client but always 
   * requested from the server. 
   */
  public CommandTagHandleImpl(
    final Long pId, final String pName, final String pDescription, 
    final String pDataType, final int pClientTimeout, 
    final Comparable<T> pMinValue, final Comparable<T> pMaxValue, 
    final String pHostName,
    final RbacAuthorizationDetails rbacAuthorizationDetails) {
    this.id = pId;
    this.name = pName;
    this.description = pDescription;
    this.dataType = pDataType;
    this.clientTimeout = pClientTimeout;
    this.minValue = pMinValue;
    this.maxValue = pMaxValue;

    this.value = null;
    this.rbacAuthorizationDetails = rbacAuthorizationDetails;
  }
  
  public static class Builder <T>{
    
    private Long id;
    private String name;
    private String description;
    private String dataType;
    private int clientTimeout;
    private Comparable<T> minValue;
    private Comparable<T> maxValue;
    private T value;
    private RbacAuthorizationDetails rbacAuthorizationDetails;
    private int sourceTimeout;
    private int execTimeout;
    private int sourceRetries;
    private Long processId;
    private HardwareAddress hardwareAddress;    
    
    public Builder(Long id) {
      this.id = id;
    }
    
    public Builder<T> name(String name) {
      this.name = name;
      return this;
    }
    
    public Builder<T> description(String description) {
      this.description = description;
      return this;
    }
    
    public Builder<T> dataType(String dataType) {
      this.dataType = dataType;
      return this;
    }
    
    public Builder<T> clientTimeout(int clientTimeout) {
      this.clientTimeout = clientTimeout;
      return this;
    }
    
    public Builder<T> minValue(Comparable<T> minValue) {
      this.minValue = minValue;
      return this;
    }
    
    public Builder<T> maxValue(Comparable<T> maxValue) {
      this.maxValue = maxValue;
      return this;
    }
    
    public Builder<T> tValue(T value) {
      this.value = value;
      return this;
    }
    
    public Builder<T> processId(Long processId) {
      this.processId = processId;
      return this;
    }
    
    public Builder<T> rbacAuthorizationDetails(RbacAuthorizationDetails rbacAuthorizationDetails) {
      this.rbacAuthorizationDetails = rbacAuthorizationDetails;
      return this;
    }
    
    public Builder<T> sourceTimeout(int sourceTimeout) {
      this.sourceTimeout = sourceTimeout;
      return this;
    }
    
    public Builder<T> execTimeout(int execTimeout) {
      this.execTimeout = execTimeout;
      return this;
    }
    
    public Builder<T> sourceRetries (int sourceRetries) {
      this.sourceRetries = sourceRetries;
      return this;
    }
    
    public Builder<T> hardwareAddress(HardwareAddress hardwareAddress) {
      this.hardwareAddress = hardwareAddress;
      return this;
    }
    
    public CommandTagHandleImpl<T> build() {
      return new CommandTagHandleImpl<T>(this);
    }
  }
  
  public CommandTagHandleImpl(Builder<T> builder) {
    
    id = builder.id;
    name = builder.name;
    description = builder.description;
    dataType = builder.dataType;
    clientTimeout = builder.clientTimeout;
    minValue = builder.minValue;
    maxValue = builder.maxValue;
    value = builder.value;
    rbacAuthorizationDetails = builder.rbacAuthorizationDetails;
    sourceTimeout = builder.sourceTimeout;
    execTimeout = builder.execTimeout;
    sourceRetries = builder.sourceRetries;
    processId = builder.processId;
    hardwareAddress = builder.hardwareAddress;
  }

  /**
   * Get the unique numeric identifier of the CommandTag represented by the 
   * present CommandTagHandle object.
   */
  public Long getId() {
    return this.id;
  }

  /**
   * Get the name of the CommandTag represented by the present CommandTagHandle
   * object.
   */
  public String getName() {
    return this.name;
  }

  /**
   * Get the (optional) free-text description of the CommandTag represented by 
   * the present CommandTagHandle object.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Get the name of the data type of the present CommandTagHandle object.
   * Only values of this data type can be set using setValue().
   */
  public String getDataType() {
    return this.dataType;
  }

  /**
   * Get the client timeout in milliseconds.
   * When a client sends a CommandTagHandle to the server for execution and 
   * has not received a CommandTagReport after 'clientTimeout' milliseconds,
   * it should consider the command execution as failed.
   */
  public int getClientTimeout() {
    return this.clientTimeout;
  }

  /**
   * Get the authorized maximum for the command value. 
   * If the client tries to set a value greater than this maximum, the 
   * setValue() method will throw a CommandTagValueException. If the maxValue 
   * is null, it is not taken into account. The maxValue will always be null
   * for non-numeric commands.
   */
  public Comparable<T> getMaxValue() {
    return this.maxValue;
  }

  /**
   * Get the authorized minimum for the command value. 
   * If the client tries to set a value less than this minimum, the 
   * setValue() method will throw a CommandTagValueException. If the minValue 
   * is null, it is not taken into account. The minValue will always be null
   * for non-numeric commands.
   */
  public Comparable<T> getMinValue() {
    return this.minValue;
  }

  /**
   * Check whether the present CommandTagHandle object represents a CommandTag
   * that exists on the server. If not, the client will not be able to 
   * execute the command. Preferably, clients should check isExistingCommand()
   * BEFORE they call the setValue() method. If the command doesn't exist,
   * setValue() will throw a CommandTagValueException.
   */
  public boolean isExistingCommand() {
    return (!name.equals(CommandTagHandle.CMD_UNKNOWN));
  }

  /**
   * Set the command value
   * This method must be called before CommandTagHandle objects are sent to the
   * server for command execution. The method will throw a CommandTagValueException
   * if one of the following conditions is met:
   * <UL>
   * <LI>the set value is null
   * <LI>the user is not authorized to execute this command
   * <LI>the present CommandTagHandle object does not represent a CommandTag that
   * exists on the server
   * <LI>the set value is not between the authorized minimum and maximum values
   */
  public void setValue(T value) throws CommandTagValueException {
    // Check the authorization ticked
//    if (!isAuthorised()) {
//      throw new CommandTagValueException(
//          "Not authorised : this CommandTagHandle has no valid authorization ticket.");
//    }

    // Check if value is NOT NULL
    if (value == null) {
      throw new CommandTagValueException(
          "Null value : command values cannot be set to null");
    }

    // Check if the data type of the set value corresponds to specified data type
    String curDataType = value.getClass().getName().substring(10);

    if (!curDataType.equals(dataType)) {
      throw new CommandTagValueException(
          "Data type : " + dataType + " expected. Cannot set value of type "
          + curDataType + ".");
    }

    try {
      if ((minValue != null) && minValue.compareTo(value) > 0) {
        throw new CommandTagValueException(
            "Out of range : " + value
            + " is less than the authorized minimum value " + minValue + ".");
      }
    }
    catch (ClassCastException ce) {
        throw new CommandTagValueException(
            "CONFIGURATION ERROR: The minValue for the command is of type " 
            + minValue.getClass().getName()
            + ". It cannot be compared to a value of type " 
            + value.getClass().getName()
            + ". Contact the configuration responsible for correcting this problem"
        );
    }

    try {
      if ((maxValue != null) && maxValue.compareTo(value) < 0) {
        throw new CommandTagValueException(
            "Out of range : " + value
            + " is greater than the authorized maximum value " + maxValue + ".");
      }
    }
    catch (ClassCastException ce) {
        throw new CommandTagValueException(
            "CONFIGURATION ERROR: The minValue for the command is of type " 
            + minValue.getClass().getName()
            + ". It cannot be compared to a value of type " 
            + value.getClass().getName()
            + ". Contact the configuration responsible for correcting this problem"
        );
    }


    this.value = value;
  }

  /**
   * Get the command's present value as set by the user.
   * This field will always be null before the user executes the setValue()
   * method.
   */
  public T getValue() {
    return this.value;
  }

  @Override
  public AuthorizationDetails getAuthorizationDetails() {
    return rbacAuthorizationDetails;
  }

  @Override
  public int getExecTimeout() {
    return execTimeout;
  }

  @Override
  public HardwareAddress getHardwareAddress() {
    return hardwareAddress;
  }

  @Override
  public Long getProcessId() {
    return processId;
  }

  @Override
  public int getSourceRetries() {
    return sourceRetries;
  }

  @Override
  public int getSourceTimeout() {
    return sourceTimeout;
  }
}
