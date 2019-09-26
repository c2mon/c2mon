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
package cern.c2mon.server.common.command;

import cern.c2mon.server.common.metadata.Metadata;
import cern.c2mon.shared.client.command.RbacAuthorizationDetails;
import cern.c2mon.shared.common.command.CommandExecutionDetails;
import cern.c2mon.shared.common.command.CommandTag;
import cern.c2mon.shared.common.datatag.address.HardwareAddress;
import lombok.Data;
import org.simpleframework.xml.Transient;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

/**
 * Note: does not keep the latest value of the command. Commands are logged
 * to the history database.
 *
 * @param <T> the type of the values that can be set for this command
 */
@Data
public final class CommandTagCacheObject<T> implements CommandTag<T> {

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
    private HardwareAddress hardwareAddress;

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
     * The meta data of the Tag. The meta data can be arbitrary and of of the type String, Number and Boolean.
     * Not every Tag needs to have a meta data. Also the meta data don't have to be every time the same.
     */
    private Metadata metadata;

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
        this.hardwareAddress = pHwAddress;
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
        this(old.id, old.name, old.description, old.dataType, old.mode, old.equipmentId, old.processId, old.hardwareAddress,
                old.sourceTimeout, old.sourceRetries, old.execTimeout, old.clientTimeout, old.minimum, old.maximum);
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

        if (hardwareAddress != null) {
            str.append(hardwareAddress.toConfigXML());
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

      if (hardwareAddress != null) {
        clone.hardwareAddress = this.hardwareAddress.clone();
      }

      return clone;
    }
}
