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
package cern.c2mon.shared.common.datatag;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementMap;
import org.simpleframework.xml.core.Persist;

import cern.c2mon.shared.common.datatag.util.TagQualityStatus;

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
    private ConcurrentHashMap<TagQualityStatus, String> invalidQualityStates = new ConcurrentHashMap<>();

    /**
     * Only used for XML serialization. The result of the isValid method is stored here.
     */
    @Element(required = false)
    private boolean isValid;

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
    private void prepare() {
        isValid = isValid();
    }

    @Override
    public DataTagQuality clone() throws CloneNotSupportedException {
        DataTagQualityImpl clone = (DataTagQualityImpl) super.clone();
        clone.invalidQualityStates = new ConcurrentHashMap<>(invalidQualityStates);
        return clone;
    }

    /**
     * @return true if the quality object represents a "valid" state --> no error conditions apply.
     */
    @Override
    public boolean isValid() {
        return invalidQualityStates.isEmpty();
    }

    @Override
    public boolean isInvalidStatusSet(final TagQualityStatus status) {
        return invalidQualityStates.containsKey(status);
    }

    @Override
    public boolean isInvalidStatusSetWithSameDescription(final TagQualityStatus status, final String qualityDescription) {
        String nonNullQualityDescription;
        if (qualityDescription == null) {
            nonNullQualityDescription = "";
        } else {
            nonNullQualityDescription = qualityDescription;
        }
        
        return invalidQualityStates.containsKey(status)
                    && nonNullQualityDescription.equalsIgnoreCase(invalidQualityStates.get(status));
    }

    @Override
    public boolean isExistingTag() {
      return !invalidQualityStates.containsKey(TagQualityStatus.UNDEFINED_TAG);
    }

    /**
     * @return true if the UNINITIALISED flag is not set.
     */
    @Override
    public boolean isInitialised() {
      return !invalidQualityStates.containsKey(TagQualityStatus.UNINITIALISED);
    }

    @Override
    public boolean isAccessible() {
      boolean accessible = true;
      if (invalidQualityStates.containsKey(TagQualityStatus.PROCESS_DOWN)
          || invalidQualityStates.containsKey(TagQualityStatus.EQUIPMENT_DOWN)
          || invalidQualityStates.containsKey(TagQualityStatus.SUBEQUIPMENT_DOWN)
          || invalidQualityStates.containsKey(TagQualityStatus.INACCESSIBLE)
          || invalidQualityStates.containsKey(TagQualityStatus.SERVER_HEARTBEAT_EXPIRED)
          || invalidQualityStates.containsKey(TagQualityStatus.JMS_CONNECTION_DOWN)) {
        accessible = false;
      }

      return accessible;
    }

    /**
     * Resets all the error conditions.
     */
    @Override
    public void validate() {
      invalidQualityStates.clear();
    }

    @Override
    public void setInvalidStates(final Map<TagQualityStatus, String> qualityStates) {
      invalidQualityStates.clear();
      if (qualityStates != null) {
        invalidQualityStates.putAll(qualityStates);
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

      return description;
    }

    @Override
    public boolean addInvalidStatus(final TagQualityStatus statusToAdd) {
        return addInvalidStatus(statusToAdd, "");
    }

    @Override
    public boolean addInvalidStatus(final TagQualityStatus statusToAdd, final String description) {
      if (statusToAdd != null) {
        if (description != null) {
          invalidQualityStates.put(statusToAdd, description);
        } else {
          invalidQualityStates.put(statusToAdd, "");
        }
        return true;
      }

      return false;
    }

    @Override
    public Map<TagQualityStatus, String> getInvalidQualityStates() {
        return new HashMap<TagQualityStatus, String>(invalidQualityStates);
    }

    @Override
    public void removeInvalidStatus(final TagQualityStatus statusToRemove) {
      if (statusToRemove != null) {
        invalidQualityStates.remove(statusToRemove);
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
