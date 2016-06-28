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
package cern.c2mon.shared.rule;

import java.io.Serializable;

public final class RuleInputTagId implements Serializable {

    private static final long serialVersionUID = -9149116674792536377L;

    /** The input tag id */
    Long id = null;

    public RuleInputTagId(final Long pId) {
        this.id = pId;
    }

    public RuleInputTagId(final String pId) throws RuleFormatException {
        if (pId == null) {
            throw new RuleFormatException("Input tag id cannot be null");
        }
        String tmpId = pId.trim();
        if (tmpId.charAt(0) == '#') {
            tmpId = tmpId.substring(1);
        }
        try {
            this.id = Long.valueOf(tmpId);
        } catch (NumberFormatException e) {
            throw new RuleFormatException("Invalid tag identifier: #" + pId);
        }
    }

    public String toString() {
       return "#" + this.id + " "; 
    }

    public final Long getId() {
        return this.id;
    }
}
