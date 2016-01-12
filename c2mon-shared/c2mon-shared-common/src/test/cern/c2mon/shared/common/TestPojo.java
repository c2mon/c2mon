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
package cern.c2mon.shared.common;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlValue;

public class TestPojo {
    private Long id = 1000L;
    private String name = "Pojo";
    private int numberTest = 0;
    private Long testNumber = 238438L;
    @XmlElement(name="Pi")
    private double aDoubleNumber = 3.1415926;
    private transient String transientString = "notInDocument"; // Will be ignored
    private String normalString = "sdjhsd";
    private String nullString; // Will be ignored
    private int notSetInt; // Will be assumed as 0
    private Integer nullInt; // Will be ignored
    private List<String> list = new ArrayList<String>();
    
    @XmlValue
    private String equipmentUnitXml = "<aaa>some internal xml block</aaa>"; // shall be wrapped inside <![CDATA[ ]]
}
