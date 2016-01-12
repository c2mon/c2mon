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

package cern.c2mon.configloader.requestor;

import java.util.List;

import cern.c2mon.configloader.Configuration;

/**
 * The <code>TestReconfigurationRequestor</code> is used for test purposes only. It extends the
 * <code>ServerReconfigurationRequestor</code> interface with a possibility to access reconfiguration records
 * 
 * @author wbuczak
 */
public interface TestReconfigurationRequestor extends ServerReconfigurationRequestor {
    List<Configuration> getReconfigurationRequests();
}
