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

    /** Data tag type */
    private String tagDataType;

    /** Data tag timestamp */
    private Timestamp tagTimestamp;

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
    public final Timestamp getTagTimestamp() {
        return tagTimestamp;
    }

    /**
     * @param tTimestamp
     *            the tagTimestamp to set
     */
    public final void setTagTimestamp(final Timestamp tTimestamp) {
        this.tagTimestamp = tTimestamp;
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

            dtShortTermLog = new TagShortTermLog();
            dtShortTermLog.setTagId(new Long(value[j++]).longValue());
            dtShortTermLog.setTagName(value[j++]);
            String tagValue = value[j++];
            if (tagValue.length() >= MAX_VALUE_LENGTH) {
                LOG.warn("The value " + tagValue + " of the tag " + dtShortTermLog.getTagId() + " has been truncated. It is too long for the database");
                dtShortTermLog.setTagValue(tagValue.substring(0, MAX_VALUE_LENGTH -1));
                 
            } else {
                dtShortTermLog.setTagValue(tagValue);    
            }            
            dtShortTermLog.setTagDataType(value[j++]);
            dtShortTermLog.setTagTimestamp(Timestamp.valueOf(value[j++]));
            dtShortTermLog.setTagQualityCode(new Integer(value[j++]).shortValue());
            String description = (String)value[j++];
            if (description.equalsIgnoreCase("null")) {
                dtShortTermLog.setTagQualityDesc("");
            } else {
                dtShortTermLog.setTagQualityDesc(description);
            }            
            dtShortTermLog.setTagMode(new Integer(value[j++]).shortValue());
            dtShortTermLog.setTagDir(value[j++]);
            dtShortTermLog.setLogDate(Timestamp.valueOf(value[j++]));
            dtShortTermLog.setTimezone(TimeZone.getDefault().getID());
        } catch (Exception e) {
            // If one of the conversions can not be done, as for example to
            // treat the IlegalArgumentException that may happen
            // when the string with the timestamp has not the correct argument
            throw new DataFallbackException(
                    "Error with the format of some of the file's lines " + value[0] + e.getMessage());
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
        str.append(getTagDataType());
        str.append('\t');
        str.append(getTagTimestamp());
        str.append('\t');
        str.append(getTagQualityCode());
        str.append('\t');
        if ((getTagQualityDesc() != null) && (getTagQualityDesc().equals(""))){
            str.append("null");
        } else {
            str.append(getTagQualityDesc());
        }        
        str.append('\t');
        str.append(getTagMode());
        str.append('\t');
        str.append("I");
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
     * Compares two objects of the same class. The objects will be equal when
     * its properties have the same values
     * 
     * @param obj
     *            DataTagShortTermLog object to which we want to compare the
     *            current one
     * @return A boolean value indicating whether the two objects are considered
     *         equals (true) or not (false)
     */
    public final boolean equals(final Object obj) {
        boolean equal = false;

        TagShortTermLog dt = (TagShortTermLog) obj;
        if ((dt.getTagId() == this.getTagId()) && dt.getTagName().equals(this.getTagName())
                && (dt.getTagDir().equals(this.getTagDir()))
                && dt.getTagMode() == this.getTagMode()
                && dt.getTagQualityCode() == this.getTagQualityCode()
                && dt.getTagQualityDesc().equals(this.getTagQualityDesc())
                && dt.getTagValue().equals(this.getTagValue())
                && dt.getTagDataType().equals(this.getTagDataType())
                && this.getTagTimestamp().equals(this.getTagTimestamp()) 
                && dt.getTagDir().equals(this.getTagDir())) {
            equal = true;
        }
        return equal;
    }

    /**
     * Overrides the hashCode method as required when overriding the "equals"
     * method.
     * 
     * @return Int value representing the hashcode of the object
     */
    public final int hashCode() {
        return this.getId().hashCode();
    }

    /**
     * Implementation for generic logging functionality (see {@link Loggable}).
     */
    @Override
    public String getValue() {
     return getTagValue();
    }

}
