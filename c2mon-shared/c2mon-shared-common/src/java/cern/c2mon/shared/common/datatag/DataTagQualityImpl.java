/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2011 CERN This program is free software; you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version. This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details. You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.shared.common.datatag;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Persist;

/**
 * The DataTagQuality is used to represent the quality attribute of a DataTag. The most important information a
 * DataTagQuality object provides is whether a tag's value is valid (<code>isValid()</code>) or not. In addition to
 * that, more fine grain information about the reason for invalidity (e.g. an acquisition error, a range-check failure
 * etc.) is available.
 * 
 * @author Matthias Braeger
 */

public final class DataTagQualityImpl implements DataTagQuality {

    private static final long serialVersionUID = 7317518750573239551L;

    /** Standard description for a valid tag */
    private static final transient String VALID_DESCR = "OK";

    /**
     * Contains the list of set invalidation states and the optional textual description.
     */
    @ElementMap(required = false)
    private HashMap<TagQualityStatus, String> invalidQualityStates = new HashMap<TagQualityStatus, String>();

    /**
     * Only used for XML serialization. The result of the isValid method is stored here.
     */
    @SuppressWarnings("unused")
    @Element(required = false)
    private boolean isValid;

    /** Read-/Write lock for the <code>invalidQualityStates</code> Map */
    private transient ReentrantReadWriteLock concurrentMapLock = new ReentrantReadWriteLock();

    /**
     * Default constructor Creates a DataQuality object representing an UNINITIALISED tag
     */
    public DataTagQualityImpl() {
        this(TagQualityStatus.UNINITIALISED);
    }

    /**
     * Constructor
     * 
     * @param status the quality code that shall be used for the new DataTagQuality object. For example,
     *            DataTagQuality.UNINITIALISED
     */
    public DataTagQualityImpl(final TagQualityStatus status) {
        this(status, "");
    }

    
    /**
     * This getter shall be used, because lock object is transient, so it may not be
     * instantiated after deserialization
     * @return lock instance 
     */
    private ReentrantReadWriteLock getLock() {
        if (concurrentMapLock == null) {
            concurrentMapLock = new ReentrantReadWriteLock();
        }
        return concurrentMapLock;
    }
    
    /**
     * Constructor
     * 
     * @param status The quality status to be initially set
     * @param description free-text description of the quality condition.
     */
    public DataTagQualityImpl(final TagQualityStatus status, final String description) {
        if (!setInvalidStatus(status, description)) {
            setInvalidStatus(TagQualityStatus.UNINITIALISED);
        }
    }

    /**
     * Copy constructor
     * 
     * @param oldQualityTag the <code>DataTagQuality</code> object serving as a "template" for the object to be created.
     * @throws NullPointerException In case that <code>oldQualityTag</code> is null
     */
    public DataTagQualityImpl(final DataTagQuality oldQualityTag) throws NullPointerException {
        if (oldQualityTag != null) {
            if (oldQualityTag.isValid()) {
                validate();
            } else {
                Map<TagQualityStatus, String> oldQualityStates = oldQualityTag.getInvalidQualityStates();
                for (Entry<TagQualityStatus, String> entry : oldQualityStates.entrySet()) {
                    addInvalidStatus(entry.getKey(), entry.getValue());
                }
            }
        } else {
            throw new NullPointerException("Copy constructor called with NULL parameter.");
        }
    }

    /**
     * Only used for serialization. The persist method is invoked before the serialization of the object. This allows
     * the object to prepare in some implementation specific way for the serialization process.
     */
    @Persist
    @SuppressWarnings("unused")
    private void prepare() {

        isValid = isValid();
    }

    @SuppressWarnings("unchecked")
    @Override
    public DataTagQuality clone() throws CloneNotSupportedException {
        DataTagQualityImpl clone = (DataTagQualityImpl) super.clone();
        clone.invalidQualityStates = (HashMap<TagQualityStatus, String>) invalidQualityStates.clone();

        return clone;
    }

    /**
     * @return true if the quality object represents a "valid" state --> no error conditions apply.
     */
    @Override
    public boolean isValid() {
        boolean valid = false;
        try {
            getLock().readLock().lock();
            valid = invalidQualityStates.isEmpty();
        } finally {
            getLock().readLock().unlock();
        }

        return valid;
    }

    @Override
    public boolean isInvalidStatusSet(final TagQualityStatus status) {
        boolean isSet = false;
        try {
            getLock().readLock().lock();
            isSet = invalidQualityStates.containsKey(status);
        } finally {
            getLock().readLock().unlock();
        }

        return isSet;
    }

    @Override
    public boolean isInvalidStatusSetWithSameDescription(final TagQualityStatus status, final String qualityDescription) {
        String nonNullQualityDescription;
        if (qualityDescription == null) {
            nonNullQualityDescription = "";
        } else {
            nonNullQualityDescription = qualityDescription;
        }
        boolean isSame = false;
        try {
            getLock().readLock().lock();
            isSame = invalidQualityStates.containsKey(status)
                    && nonNullQualityDescription.equalsIgnoreCase(invalidQualityStates.get(status));
        } finally {
            getLock().readLock().unlock();
        }
        return isSame;
    }

    @Override
    public boolean isExistingTag() {
        boolean exists = false;
        try {
            getLock().readLock().lock();
            exists = !invalidQualityStates.containsKey(TagQualityStatus.UNDEFINED_TAG);
        } finally {
            getLock().readLock().unlock();
        }
        return exists;
    }

    /**
     * @return true if the UNINITIALISED flag is not set.
     */
    @Override
    public boolean isInitialised() {
        boolean uninitialised = false;
        try {
            getLock().readLock().lock();
            uninitialised = invalidQualityStates.containsKey(TagQualityStatus.UNINITIALISED);
        } finally {
            getLock().readLock().unlock();
        }
        return !uninitialised;
    }

    @Override
    public boolean isAccessible() {
        boolean accessible = true;
        try {
            getLock().readLock().lock();
            if (invalidQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)
                    || invalidQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)
                    || invalidQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)
                    || invalidQualityStates.containsKey(TagQualityStatus.INACCESSIBLE)
                    || invalidQualityStates.containsKey(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED)
                    || invalidQualityStates.containsKey(TagQualityStatus.JMS_CONNECTION_DOWN)) {
                accessible = false;
            }
        } finally {
            getLock().readLock().unlock();
        }

        return accessible;
    }

    /**
     * Resets all the error conditions.
     */
    @Override
    public void validate() {
        try {
            getLock().writeLock().lock();
            invalidQualityStates.clear();
        } finally {
            getLock().writeLock().unlock();
        }
    }

    @Override
    public void setInvalidStates(final Map<TagQualityStatus, String> qualityStates) {
        try {
            getLock().writeLock().lock();
            invalidQualityStates.clear();
            if (qualityStates != null) {
                invalidQualityStates.putAll(qualityStates);
            }
        } finally {
            getLock().writeLock().unlock();
        }

    }

    @Override
    public boolean setInvalidStatus(final TagQualityStatus status) {
        return setInvalidStatus(status, "");
    }

    @Override
    public boolean setInvalidStatus(final TagQualityStatus status, final String description) {
        if (status != null) {
            validate();
            return addInvalidStatus(status, description);
        }

        return false;
    }

    @Override
    public String getDescription() {
        String description = "";
        try {
            getLock().readLock().lock();
            if (invalidQualityStates.isEmpty()) {
                description = VALID_DESCR;
            } else {
                int severity = 999; // initialized with lowest severity

                for (TagQualityStatus status : invalidQualityStates.keySet()) {

                    if (status.getSeverity() < severity) {
                        description = invalidQualityStates.get(status).trim();
                        severity = status.getSeverity();
                    } else if (status.getSeverity() == severity) {
                        description += "; " + invalidQualityStates.get(status).trim();
                    }
                }
            }
        } finally {
            getLock().readLock().unlock();
        }

        return description;
    }

    @Override
    public boolean addInvalidStatus(final TagQualityStatus statusToAdd) {
        return addInvalidStatus(statusToAdd, "");
    }

    @Override
    public boolean addInvalidStatus(final TagQualityStatus statusToAdd, final String description) {
        if (statusToAdd != null) {
            try {
                getLock().writeLock().lock();
                if (description != null) {
                    invalidQualityStates.put(statusToAdd, description);
                } else {
                    invalidQualityStates.put(statusToAdd, "");
                }
            } finally {
                getLock().writeLock().unlock();
            }
            return true;
        }

        return false;
    }

    @Override
    public Map<TagQualityStatus, String> getInvalidQualityStates() {
        Map<TagQualityStatus, String> copy;
        try {
            getLock().readLock().lock();
            copy = new HashMap<TagQualityStatus, String>(invalidQualityStates);
        } finally {
            getLock().readLock().unlock();
        }

        return copy;
    }

    @Override
    public void removeInvalidStatus(final TagQualityStatus statusToRemove) {
        if (statusToRemove != null) {
            try {
                getLock().writeLock().lock();
                invalidQualityStates.remove(statusToRemove);
            } finally {
                getLock().writeLock().unlock();
            }
        }
    }

    /**
     * Generates a String representation of the quality code. In case that there are several error bit set the string
     * will consist of a concatenation of these error codes which are separated by a <code>'+'</code>. <br>
     * <br>
     * The resulting string is either <code>"OK"</code> or a combination of the <code>TagQualityStatus</code> codes. <br>
     * E.g.: <code>UNINITIALISED+INACCESSIBLE</code>
     */
    @Override
    public String toString() {
        StringBuilder qualityStatusStr = new StringBuilder();
        final String separator = "+";

        try {
            getLock().readLock().lock();
            if (invalidQualityStates.isEmpty()) {
                qualityStatusStr.append(VALID_DESCR);
            } else {
                boolean firstInsert = true;
                for (TagQualityStatus status : invalidQualityStates.keySet()) {
                    if (!firstInsert) {
                        qualityStatusStr.append(separator);
                    }
                    qualityStatusStr.append(status);
                    firstInsert = false;
                }
            }
        } finally {
            getLock().readLock().unlock();
        }

        return qualityStatusStr.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((invalidQualityStates == null) ? 0 : invalidQualityStates.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof DataTagQualityImpl)) {
            return false;
        }
        DataTagQualityImpl otherTagQuality = (DataTagQualityImpl) obj;
        if (invalidQualityStates == null) {
            if (otherTagQuality.invalidQualityStates != null) {
                return false;
            }
        } else if (!invalidQualityStates.equals(otherTagQuality.invalidQualityStates)) {
            return false;
        }
        return true;
    }
}
