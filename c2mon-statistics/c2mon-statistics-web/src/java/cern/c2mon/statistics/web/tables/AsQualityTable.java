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
package cern.c2mon.statistics.web.tables;

import java.util.ArrayList;
import java.util.List;

public class AsQualityTable {
    /**
     * The codes to put in the table.
     */
    private List<QualityCode> qualityCodes;
    
    /**
     * Table Constructor.
     */
    public AsQualityTable() {
        qualityCodes = new ArrayList<QualityCode>();
        qualityCodes.add(new QualityCode(0,"OK","the value is valid (all other codes are invalidity codes)"));
        qualityCodes.add(new QualityCode(1,"UNINITIALISED","no value has ever been received for this tag"));
        qualityCodes.add(new QualityCode(2,"INACCESSIBLE","the data source is inaccessible - the value could be outdated"));
        qualityCodes.add(new QualityCode(4,"VALUE_EXPIRED","the time-to-live of the tag has been reached - the value is considered outdated"));
        qualityCodes.add(new QualityCode(8,"VALUE_OUT_OF_BOUNDS","the value received is outside the min-max range"));
        qualityCodes.add(new QualityCode(16,"INVALID_TAG","the application server does not recognise the tag"));
        qualityCodes.add(new QualityCode(32,"VALUE_UNDEFINED","the value of the tag cannot be determined"));
        qualityCodes.add(new QualityCode(64,"UNKNOWN","the invalidity reason can not be identified or does not fit into any of the above categories"));
        
    }

    /**
     * @return the qualityCodes
     */
    public List<QualityCode> getQualityCodes() {
        return qualityCodes;
    }

    /**
     * @param qualityCodes the qualityCodes to set
     */
    public void setQualityCodes(List<QualityCode> qualityCodes) {
        this.qualityCodes = qualityCodes;
    }
    
}
