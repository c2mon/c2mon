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

package cern.c2mon.daq.jmx;

import cern.c2mon.patterncache.Cachable;

/**
 * The <code>UserCredentials</code> class represents user credentials.
 * 
 * @author wbuczak
 */
public class UserCredentials implements Cachable {

    private String userName;
    private String userPasswd;

    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return Returns the userPasswd.
     */
    public String getUserPasswd() {
        return userPasswd;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((userPasswd == null) ? 0 : userPasswd.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserCredentials other = (UserCredentials) obj;

        if (userName == null) {
            if (other.userName != null)
                return false;
        } else if (!userName.equals(other.userName))
            return false;
        if (userPasswd == null) {
            if (other.userPasswd != null)
                return false;
        } else if (!userPasswd.equals(other.userPasswd))
            return false;
        return true;
    }

    @Override
    public void init(String... tokens) {
        this.userName = tokens[0];
        this.userPasswd = tokens[1];
    }
}
