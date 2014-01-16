/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2005-2011 CERN. This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.server.common.command;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.simpleframework.xml.Transient;

import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.Cacheable;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import cern.c2mon.shared.daq.command.CommandExecutionDetails;
import cern.c2mon.shared.daq.command.CommandTag;

/**
 * Note: does not keep the latest value of the command. Commands are logged in the STL account.
 * 
 * @param <T> the type of the values that can be set for this command
 */

public final class CommandTagCacheObject<T> implements CommandTag<T>, Cacheable, Cloneable {

    private static final long serialVersionUID = -5348795528961997767L;

    /**
     * Unique numeric identifier of the CommandTag
     */
    private Long id;

    /**
     * Unique name of the CommandTag
     */
    private String name;

    /**
     * Optional free-text description of the CommandTag
     */
    private String description;

    /**
     * Data type of the CommandTag's values. Values of any other data type will be rejected.
     */
    private String dataType;

    /**
     * Current mode of the CommandTag (operational, maintenance or test)
     */
    private short mode;

    /**
     * Client timeout of the CommandTag
     */
    private int clientTimeout;

    /**
     * Execution timeout of the CommandTag
     */
    private int execTimeout;

    /**
     * Source timeout of the CommandTag
     */
    private int sourceTimeout;

    /**
     * Number of times a data source should retry to execute a command in case an attempted execution fails.
     */
    private int sourceRetries;

    /**
     * Unique identifier of the equipment unit the CommandTag is attached to.
     */
    private Long equipmentId;

    /**
     * Unique identifier of the process the CommandTag is attached to (via the equipment unit or one of its parents)
     */
    private Long processId;

    /**
     * Hardware address of the CommandTag. The Hardware address is required by the data source to actually execute the
     * command.
     */
    private HardwareAddress hwAddress;

    /**
     * Minimum value for the command value.
     */
    private Comparable<T> minimum;

    /**
     * Maximum value for the command value.
     */
    private Comparable<T> maximum;

    /**
     * Authorization details.
     */
    private RbacAuthorizationDetails authorizationDetails;

    /**
     * Details concerning a command execution. This is only set in the copy of the object (outside the cache), once a
     * command execution is taking place (used for instance when logging the command). It is not saved in the cache.
     */
    @Transient
    private CommandExecutionDetails<T> commandExecutionDetails;

    /**
     * Synchronization lock
     */
    private ReentrantReadWriteLock aliveLock = new ReentrantReadWriteLock();
    private ReadLock readLock = aliveLock.readLock();
    private WriteLock writeLock = aliveLock.writeLock();

    /**
     * Constructor with the minimal fields excepted to be non-null in all cache objects circulating in the server.
     * <p>
     * Used when loading the cache from the DB.
     * 
     * @param pId
     * @param name
     * @param description
     * @param dataType
     * @param mode
     */
    public CommandTagCacheObject(final Long id, final String name, final String description, final String dataType,
            final Short mode) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dataType = dataType;
        this.mode = mode;
    }

    /**
     * Constructor should only be used for testing.
     * 
     * @param pId id of command
     */
    public CommandTagCacheObject(final Long pId) {
        this(pId, null, null, null, new Short("0"));
    }

    public CommandTagCacheObject(final Long pId, final String pName, final String pDescription, final String pDataType,
            final short pMode, final Long pEquipmentId, final Long pProcessId, final HardwareAddress pHwAddress,
            final int pSourceTimeout, final int pSourceRetries, final int pExecTimeout, final int pClientTimeout,
            final Comparable<T> pMinimum, final Comparable<T> pMaximum) {
        this.id = pId;
        this.name = pName;
        this.description = pDescription;

        this.dataType = pDataType;
        this.mode = pMode;
        this.hwAddress = pHwAddress;
        this.equipmentId = pEquipmentId;
        this.processId = pProcessId;

        this.sourceTimeout = pSourceTimeout;
        this.sourceRetries = pSourceRetries;
        this.execTimeout = pExecTimeout;
        this.clientTimeout = pClientTimeout;

        this.minimum = pMinimum;
        this.maximum = pMaximum;
    }

    public CommandTagCacheObject(final CommandTagCacheObject<T> old) {
        this(old.id, old.name, old.description, old.dataType, old.mode, old.equipmentId, old.processId, old.hwAddress,
                old.sourceTimeout, old.sourceRetries, old.execTimeout, old.clientTimeout, old.minimum, old.maximum);
    }

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public String getDataType() {
        return this.dataType;
    }

    @Override
    public short getMode() {
        return this.mode;
    }

    @Override
    public int getClientTimeout() {
        return this.clientTimeout;
    }

    @Override
    public int getExecTimeout() {
        return this.execTimeout;
    }

    @Override
    public int getSourceTimeout() {
        return this.sourceTimeout;
    }

    @Override
    public int getSourceRetries() {
        return this.sourceRetries;
    }

    @Override
    public HardwareAddress getHardwareAddress() {
        return this.hwAddress;
    }

    @Override
    public Long getEquipmentId() {
        return this.equipmentId;
    }

    @Override
    public Long getProcessId() {
        return this.processId;
    }

    public void setEquipmentId(final Long newEquipmentId) {
        this.equipmentId = newEquipmentId;
    }

    public void setProcessId(final Long newProcessId) {
        this.processId = newProcessId;
    }

    @Override
    public Comparable<T> getMinimum() {
        return this.minimum;
    }

    public void setMinimum(final Comparable<T> newMinimum) {
        this.minimum = newMinimum;
    }

    @Override
    public Comparable<T> getMaximum() {
        return this.maximum;
    }

    public void setMaximum(final Comparable<T> newMaximum) {
        this.maximum = newMaximum;
    }

    @Override
    public String toConfigXML() {
        StringBuffer str = new StringBuffer("    <CommandTag id=\"");

        str.append(id);
        str.append("\" name=\"");
        str.append(name);

        str.append("      <mode>");
        str.append(mode);
        str.append("</mode>\n");

        str.append("      <datatype>");
        str.append(dataType);
        str.append("</datatype>\n");

        if (hwAddress != null) {
            str.append(hwAddress.toConfigXML());
        }

        str.append("    </CommandTag>\n");
        return str.toString();
    }

    public String toDaqXML(final Object pValue) {
        StringBuffer str = new StringBuffer("<CommandTag id=\"");
        str.append(this.id);
        str.append("\" name=\"");
        str.append(this.name);
        str.append("\" equipment-id=\"");
        str.append(this.equipmentId);
        str.append("\">\n");
        str.append("  <value type=\"");
        str.append(this.dataType);
        str.append("\">");
        str.append(pValue);
        str.append("</value>\n");
        str.append("  <mode>");
        str.append(this.mode);
        str.append("</mode>\n");
        str.append("</CommandTag>\n");

        return str.toString();
    }

    /**
     * @param hwAddress the hwAddress to set
     */
    public void setHardwareAddress(final HardwareAddress hwAddress) {
        this.hwAddress = hwAddress;
    }

    /**
     * @param clientTimeout the clientTimeout to set
     */
    public void setClientTimeout(final int clientTimeout) {
        this.clientTimeout = clientTimeout;
    }

    /**
     * @param execTimeout the execTimeout to set
     */
    public void setExecTimeout(final int execTimeout) {
        this.execTimeout = execTimeout;
    }

    /**
     * @param sourceTimeout the sourceTimeout to set
     */
    public void setSourceTimeout(final int sourceTimeout) {
        this.sourceTimeout = sourceTimeout;
    }

    /**
     * @param sourceRetries the sourceRetries to set
     */
    public void setSourceRetries(final int sourceRetries) {
        this.sourceRetries = sourceRetries;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(final String dataType) {
        this.dataType = dataType;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(final short mode) {
        this.mode = mode;
    }

    @Override
    public RbacAuthorizationDetails getAuthorizationDetails() {
        return authorizationDetails;
    }

    /**
     * @param authorizationDetails the authorizationDetails to set
     */
    public void setAuthorizationDetails(final RbacAuthorizationDetails authorizationDetails) {
        this.authorizationDetails = authorizationDetails;
    }

    /**
     * @return the commandExecutionDetails
     */
    @Override
    public CommandExecutionDetails<T> getCommandExecutionDetails() {
        return commandExecutionDetails;
    }

    /**
     * @param commandExecutionDetails the commandExecutionDetails to set
     */
    @Override
    public void setCommandExecutionDetails(final CommandExecutionDetails<T> commandExecutionDetails) {
        this.commandExecutionDetails = commandExecutionDetails;
    }

    @Override
    public CommandTagCacheObject<T> clone() throws CloneNotSupportedException {
      @SuppressWarnings("unchecked")
      CommandTagCacheObject<T> clone = (CommandTagCacheObject<T>) super.clone();
      
      clone.aliveLock = new ReentrantReadWriteLock();
      clone.readLock = clone.aliveLock.readLock();
      clone.writeLock = clone.aliveLock.writeLock();
      
      if (authorizationDetails != null) {
        clone.authorizationDetails = this.authorizationDetails.clone();
      }

      clone.commandExecutionDetails = null;
      
      if (hwAddress != null) {
        clone.hwAddress = this.hwAddress.clone();
      }
      
      return clone;
    }
}
