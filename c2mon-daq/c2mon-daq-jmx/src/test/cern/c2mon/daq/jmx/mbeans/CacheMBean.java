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
package cern.c2mon.daq.jmx.mbeans;

import java.util.List;
import java.util.Map;

public interface CacheMBean {

    int getCacheSize();

    void setCacheSize(int size);

    List<String> getCacheListeners();

    List<String> getValues();

    void setCachedObjects(int objects);

    int getCachedObjects();

    long getCurrentSrvTime();

    String test1();

    String test2(boolean condition);

    String test2(Integer val);

    Map<String, Integer> getTestMap();

    void setValues(List<String> newVals);

    void setTestMap(Map<String, Integer> newElements);
}
