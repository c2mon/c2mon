/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.patterncache;

import cern.c2mon.patterncache.Cachable;

/**
 * The <code>UserCredentials</code> class represents user credentials. This class is for demonstration purposes only. It
 * is an example implementation of the pattern-cache's <code>Cachable</code> interface
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
