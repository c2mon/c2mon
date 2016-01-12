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
package cern.c2mon.publisher.rdaAlarms;
/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */


import cern.cmw.rda3.client.core.AccessPoint;
import cern.cmw.rda3.client.service.ClientService;
import cern.cmw.rda3.common.Rda3Factory;
import cern.cmw.rda3.common.data.AcquiredData;
import cern.cmw.rda3.common.exception.RdaException;

 
public class GetUsingRda {
    
    public static final String DEVICE = "DMN.RDA.ALARMS";
    public static final String ALARM_SOURCE = "TIMOPALARM";
    
    public static void main(final String[] args) {
        ClientService client = null;
        try {
 
            // Create a new client service. If more than one RDA3 call is performed it is recommended to keep the client
            // service object. Creating a new client service is heavy and it establishes a new remote connection to
            // the server.
            System.out.println("Create client ...");
            client = Rda3Factory.createClientService();
 
            // Create the access point which is an handle to a device/property. Access points are cached within the
            // client service so it is not needed to keep access point instances.
            System.out.println("Create access point ...");
            AccessPoint accessPoint = client.getAccessPoint(DEVICE, ALARM_SOURCE);
 
            // Perform a get call on the access point. This will throw an exception if the server is down or the
            // equipment cannot acquire the data. The result contains the data and meta information (cycle, timestamps).
            System.out.println("Now get ...");
            AcquiredData result = accessPoint.get();
 
            // Print the result.           
            System.out.println("context:\n" + result.getContext());
            System.out.println("data:\n" + result.getData());    
        
        } catch (RdaException ex) {
            ex.printStackTrace();
        } finally {
            if (client != null) {              
                client.close();
            }
        }
        System.out.println("Halt.");
        System.exit(0);
    }
}
