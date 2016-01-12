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

public class EquipmentDef {
   
    private Long ruleTagId;
    private String name;
    private String type;
       
    public long getRuleTagId() {
        return ruleTagId == null ? 0 : ruleTagId;
    }

    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    
    public EquipmentDef(final Long ruleTagId, final String name, final String type) {
        this.ruleTagId = ruleTagId;
        this.name = name;
        this.type = type;
    }    
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof EquipmentDef))
            return false;

        EquipmentDef other = (EquipmentDef) obj;
        return new EqualsBuilder().append(this.ruleTagId, other.ruleTagId).append(this.name, other.name).append(
                this.type, other.type).isEquals();
    }
    
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder(3, 21).append(this.ruleTagId).append(this.name).append(this.type)
                .hashCode();
    }    
    
}
