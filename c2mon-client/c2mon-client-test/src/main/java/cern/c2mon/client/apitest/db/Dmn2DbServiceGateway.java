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
package cern.c2mon.client.apitest.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import cern.c2mon.client.apitest.service.C2MonClientApiTestService;


public class Dmn2DbServiceGateway {

    /** Class logger */
    private static final Logger LOGGER = LoggerFactory.getLogger(Dmn2DbServiceGateway.class);

    private static Dmn2DbServiceGateway theInstance = null;

    private C2MonClientApiTestService dbAccessService;

    /**
     * Hidden constructor
     */
    private Dmn2DbServiceGateway(C2MonClientApiTestService dbService) {
        this.dbAccessService = dbService;
    }

    public static C2MonClientApiTestService getDbAccessService() {
        if (theInstance == null) {
            throw new IllegalStateException(
                    "Dmn2DbServiceGateway is not initialized. Call init() first");
        }

        return theInstance.dbAccessService;
    }

    
    public static void init() {
        if (theInstance == null) {
            LOGGER.info("loading application context from classpath:application-context.xml");
            final ClassPathXmlApplicationContext xmlContext = 
                new ClassPathXmlApplicationContext("classpath:application-context.xml"); 
                    
            theInstance = new Dmn2DbServiceGateway(xmlContext.getBean(C2MonClientApiTestService.class));
        }
                
    }
    
}
