/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader;

import static org.junit.Assert.assertEquals;
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

import cern.c2mon.configloader.C2MonConfigLoaderConfig;

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
    }

}