/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/

package cern.c2mon.server.shorttermlog.structure;

import java.sql.Timestamp;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;

/**
 * JavaBean that represents a row from the shorttermlog database table
 * 
 * @author M. Ruiz
 * @version $Revision: 1.11 $ ($Date: 2009/09/04 10:10:43 $ - $State: Exp $)
 */
public class TagShortTermLog implements IFallback, Loggable {

    private static Logger LOG = Logger.getLogger(TagShortTermLog.class);
    
    /** Data tag value */
    private String tagValue;
    
    /** Tag value description */
    private String tagValueDesc;

    /** Data tag type */
    private String tagDataType;

    /** Data tag timestamp */
    private Timestamp sourceTimestamp;
    
    /** Timestamp set when sent from DAQ */
    private Timestamp daqTimestamp;
    
    /** Timestamp when written to cache */
    private Timestamp serverTimestamp;    

    /** Code that shows whether the quality of the datatag is good or not */
    private int tagQualityCode;

    /** Description for the datatag quality code */
    private String tagQualityDesc;

    /** Data tag mode */
    private short tagMode;

    /** Log date from the database */
    private Timestamp logDate = null;

    /** Data Tag Id */
    private long tagId;

    /** Data Tag Name */
    private String tagName;

    /** Data Tag Dir */
    private String tagDir;

    /** Indicates the timezone in which the system is running */
    private String timezone;
    
    /** Constant indicating the maximum lenght that the field tagValue can reach **/
    private static int MAX_VALUE_LENGTH = 4000; 

    /**
     * @return the timezone
     */
    public final String getTimezone() {
        return timezone;
    }

    /**
     * @param tmzone
     *            the tmzone to set
     */
    public final void setTimezone(final String tmzone) {
        this.timezone = tmzone;
    }

    /**
     * @return the tagValue
     */
    public final String getTagValue() {
        return tagValue;
    }

    /**
     * @param tValue
     *            the tagValue to set
     */
    public final void setTagValue(final String tValue) {
        this.tagValue = tValue;
    }

    /**
     * @return the tagDataType
     */
    public final String getTagDataType() {
        return tagDataType;
    }

    /**
     * @param tDataType
     *            the tagDataType to set
     */
    public final void setTagDataType(final String tDataType) {
        this.tagDataType = tDataType;
    }

    /**
     * @return the tagTimestamp
     */
    public final Timestamp getSourceTimestamp() {
        return sourceTimestamp;
    }

    /**
     * @param tTimestamp
     *            the tagTimestamp to set
     */
    public final void setSourceTimestamp(final Timestamp tTimestamp) {
        this.sourceTimestamp = tTimestamp;
    }

    /**
     * @return the tagQualityCode
     */
    public final int getTagQualityCode() {
        return tagQualityCode;
    }

    /**
     * @param tQualityCode
     *            the tagQualityCode to set
     */
    public final void setTagQualityCode(final int tQualityCode) {
        this.tagQualityCode = tQualityCode;
    }

    /**
     * @return the tagQualityDesc
     */
    public final String getTagQualityDesc() {
        return tagQualityDesc;
    }

    /**
     * @param tQualityDesc
     *            the tagQualityDesc to set
     */
    public final void setTagQualityDesc(final String tQualityDesc) {
        this.tagQualityDesc = tQualityDesc;
    }

    /**
     * @return the tagMode
     */
    public final short getTagMode() {
        return tagMode;
    }

    /**
     * @param tMode
     *            the tagMode to set
     */
    public final void setTagMode(final short tMode) {
        this.tagMode = tMode;
    }

    /**
     * @return the logDate
     */
    public final Timestamp getLogDate() {
        return logDate;
    }

    /**
     * @param lDate
     *            the logDate to set
     */
    public final void setLogDate(final Timestamp lDate) {
        this.logDate = lDate;
    }

    /**
     * @return the tagId
     */
    public final long getTagId() {
        return tagId;
    }

    /**
     * @param tId
     *            the tagId to set
     */
    public final void setTagId(final long tId) {
        this.tagId = tId;
    }

    /**
     * @return the tagName
     */
    public final String getTagName() {
        return tagName;
    }

    /**
     * @param tName
     *            the tagName to set
     */
    public final void setTagName(final String tName) {
        this.tagName = tName;
    }

    /**
     * @return the tagDir
     */
    public final String getTagDir() {
        return tagDir;
    }

    /**
     * @param tDir
     *            the tagDir to set
     */
    public final void setTagDir(final String tDir) {
        this.tagDir = tDir;
    }

        /**
     * Converts a string into a DataTagShortTermLog object
     * 
     * @param object
     *            The string containing the relevant information
     * @return A DataTagShortTermLog object populated with the info contained in
     *         the string
     * @throws DataFallbackException
     *             An exception is thrown in case that there were some type
     *             issues while transforming the string into an object
     */
    public final IFallback getObject(final String object) throws DataFallbackException {
        String[] value = object.split("\t");
        int j = 0;

        TagShortTermLog dtShortTermLog;
        try {
            String currentValue;
            dtShortTermLog = new TagShortTermLog();
            dtShortTermLog.setTagId(new Long(value[j++]).longValue());
            dtShortTermLog.setTagName(value[j++]);
            currentValue = value[j++];
            String tagValue = currentValue.equals("null") ? null : currentValue;
            if (tagValue != null && tagValue.length() >= MAX_VALUE_LENGTH) {
                LOG.warn("The value " + tagValue + " of the tag " + dtShortTermLog.getTagId() + " has been truncated. It is too long for the database");
                dtShortTermLog.setTagValue(tagValue.substring(0, MAX_VALUE_LENGTH -1));
                 
            } else {
                dtShortTermLog.setTagValue(tagValue);    
            } 
            currentValue = value[j++];
            dtShortTermLog.setTagValueDesc(currentValue.equals("null") ? null : currentValue);            
            dtShortTermLog.setTagDataType(value[j++]);
            currentValue = value[j++];
            dtShortTermLog.setSourceTimestamp(currentValue.equals("null") ? null : Timestamp.valueOf(currentValue));
            currentValue = value[j++];
            dtShortTermLog.setDaqTimestamp(currentValue.equals("null") ? null : Timestamp.valueOf(currentValue));
            currentValue = value[j++];
            dtShortTermLog.setServerTimestamp(currentValue.equals("null") ? null : Timestamp.valueOf(currentValue));            
            dtShortTermLog.setTagQualityCode(new Integer(value[j++]).shortValue());
            String description = (String)value[j++];
            if (description != null && description.equalsIgnoreCase("null")) {
                dtShortTermLog.setTagQualityDesc("");
            } else {
                dtShortTermLog.setTagQualityDesc(description);
            }
            currentValue = value[j++];
            dtShortTermLog.setTagMode(currentValue.equals("null") ? null : new Integer(currentValue).shortValue());
            dtShortTermLog.setTagDir(value[j++]);
            currentValue = value[j++];
            dtShortTermLog.setLogDate(currentValue.equals("null") ? null : Timestamp.valueOf(currentValue));
            dtShortTermLog.setTimezone(TimeZone.getDefault().getID());
        } catch (Exception e) {
            // If one of the conversions can not be done, as for example to
            // treat the IlegalArgumentException that may happen
            // when the string with the timestamp has not the correct argument
            LOG.error("Error while decoding object from file", e);
            throw new DataFallbackException(
                    "Error with the format of some of the file's lines (id: " + value[0] + ") - " + e.getMessage());
        }
        return dtShortTermLog;
    }

    /**
     * Converts a DataTagShortTermLog object into a string representation
     * 
     * @return The string representation of the object
     */
    public final String toString() {
        StringBuffer str = new StringBuffer();
        str.append(getTagId());
        str.append('\t');
        str.append(getTagName());
        str.append('\t');
        if (getTagValue() != null) {
            str.append(getTagValue());
        } else {
            str.append("null");
        }
        str.append('\t');
        str.append(getTagValueDesc());
        str.append('\t');
        str.append(getTagDataType());
        str.append('\t');
        str.append(getSourceTimestamp());
        str.append('\t');
        str.append(getDaqTimestamp());
        str.append('\t');
        str.append(getServerTimestamp());
        str.append('\t');
        str.append(getTagQualityCode());
        str.append('\t');
        if ((getTagQualityDesc() != null) && (getTagQualityDesc().equals(""))) {
            str.append("null");
        } else {
            str.append(getTagQualityDesc());
        }        
        str.append('\t');
        str.append(getTagMode());
        str.append('\t');
        str.append(getTagDir());
        str.append('\t');
        str.append(new Timestamp(System.currentTimeMillis()));
        return str.toString();
    }
    
    /**
     * Returns the object identifier
     * @return The dataTag id
     */
    public final String getId() {
        return String.valueOf(this.getTagId());
    }    

    /**
     * Implementation for generic logging functionality (see {@link Loggable}).
     */
    @Override
    public String getValue() {
     return getTagValue();
    }

    /**
     * @return the daqTimestamp
     */
    public Timestamp getDaqTimestamp() {
      return daqTimestamp;
    }

    /**
     * @param daqTimestamp the daqTimestamp to set
     */
    public void setDaqTimestamp(Timestamp daqTimestamp) {
      this.daqTimestamp = daqTimestamp;
    }

    /**
     * @return the serverTimestamp
     */
    public Timestamp getServerTimestamp() {
      return serverTimestamp;
    }

    /**
     * @param serverTimestamp the serverTimestamp to set
     */
    public void setServerTimestamp(Timestamp serverTimestamp) {
      this.serverTimestamp = serverTimestamp;
    }

    /**
     * @param tagValueDesc the value description to set
     */
    public void setTagValueDesc(String tagValueDesc) {
      this.tagValueDesc = tagValueDesc;
    }

    /**
     * @return Tag value description
     */
    public String getTagValueDesc() {
      return tagValueDesc;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((daqTimestamp == null) ? 0 : daqTimestamp.hashCode());
      result = prime * result + ((logDate == null) ? 0 : logDate.hashCode());
      result = prime * result + ((serverTimestamp == null) ? 0 : serverTimestamp.hashCode());
      result = prime * result + ((sourceTimestamp == null) ? 0 : sourceTimestamp.hashCode());
      result = prime * result + ((tagDataType == null) ? 0 : tagDataType.hashCode());
      result = prime * result + ((tagDir == null) ? 0 : tagDir.hashCode());
      result = prime * result + (int) (tagId ^ (tagId >>> 32));
      result = prime * result + tagMode;
      result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
      result = prime * result + tagQualityCode;
      result = prime * result + ((tagQualityDesc == null) ? 0 : tagQualityDesc.hashCode());
      result = prime * result + ((tagValue == null) ? 0 : tagValue.hashCode());
      result = prime * result + ((tagValueDesc == null) ? 0 : tagValueDesc.hashCode());
      return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      TagShortTermLog other = (TagShortTermLog) obj;
      if (daqTimestamp == null) {
        if (other.daqTimestamp != null)
          return false;
      } else if (!daqTimestamp.equals(other.daqTimestamp))
        return false;
      if (logDate == null) {
        if (other.logDate != null)
          return false;
      } else if (!logDate.equals(other.logDate))
        return false;
      if (serverTimestamp == null) {
        if (other.serverTimestamp != null)
          return false;
      } else if (!serverTimestamp.equals(other.serverTimestamp))
        return false;
      if (sourceTimestamp == null) {
        if (other.sourceTimestamp != null)
          return false;
      } else if (!sourceTimestamp.equals(other.sourceTimestamp))
        return false;
      if (tagDataType == null) {
        if (other.tagDataType != null)
          return false;
      } else if (!tagDataType.equals(other.tagDataType))
        return false;
      if (tagDir == null) {
        if (other.tagDir != null)
          return false;
      } else if (!tagDir.equals(other.tagDir))
        return false;
      if (tagId != other.tagId)
        return false;
      if (tagMode != other.tagMode)
        return false;
      if (tagName == null) {
        if (other.tagName != null)
          return false;
      } else if (!tagName.equals(other.tagName))
        return false;
      if (tagQualityCode != other.tagQualityCode)
        return false;
      if (tagQualityDesc == null) {
        if (other.tagQualityDesc != null)
          return false;
      } else if (!tagQualityDesc.equals(other.tagQualityDesc))
        return false;
      if (tagValue == null) {
        if (other.tagValue != null)
          return false;
      } else if (!tagValue.equals(other.tagValue))
        return false;
      if (tagValueDesc == null) {
        if (other.tagValueDesc != null)
          return false;
      } else if (!tagValueDesc.equals(other.tagValueDesc))
        return false;
      return true;
    }

}
