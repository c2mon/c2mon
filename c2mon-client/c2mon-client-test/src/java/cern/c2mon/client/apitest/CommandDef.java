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
package cern.c2mon.client.apitest;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class CommandDef {

    private Long commandTagId;
    private String uniqueName;
    private String commandType;
    private String dataType;

    public long getCommandTagId() {
        return commandTagId == null ? 0 : commandTagId;
    }

    public String getUniqueName() {
        return uniqueName;
    }

    public String getCommandType() {
        return commandType;
    }

    public String getDataType() {
        return dataType;
    }

    public CommandDef(final Long commandTagId, final String uniqueName, final String commandType, final String dataType) {
        this.commandTagId = commandTagId;
        this.uniqueName = uniqueName;
        this.commandType = commandType;
        this.dataType = dataType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CommandDef))
            return false;

        CommandDef other = (CommandDef) obj;
        return new EqualsBuilder().append(this.commandTagId, other.commandTagId).append(this.uniqueName,
                other.uniqueName).append(this.commandType, other.commandType).append(this.dataType, other.dataType)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 21).append(this.dataType).append(this.commandType).hashCode();
    }
}
