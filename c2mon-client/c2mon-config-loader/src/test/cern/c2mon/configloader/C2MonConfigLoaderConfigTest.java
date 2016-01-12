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

package cern.c2mon.configloader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/c2mon-configloader-config.xml")
@ActiveProfiles({ "TEST" })
public class C2MonConfigLoaderConfigTest {

    Logger LOG = LoggerFactory.getLogger(C2MonConfigLoaderConfigTest.class);

    @Resource
    C2MonConfigLoaderConfig config;

    @Test
    public void testBasicInterface() {
        assertNotNull(config);

        assertEquals("jdbc:oracle:thin:@devdb11", config.getDbUrl());
        assertNull(config.getDbPassword());
        assertEquals("dmntest", config.getDbUserName());
        assertEquals("C2MON_CONFIG", config.getDbConfigTableName());
        assertEquals(2, config.getDbPollingPeriod());
        assertEquals("C2MONCONFLOADER", config.getLoaderUserName());
        assertFalse(config.isJmxBootstrapEnabled());
    }

}
