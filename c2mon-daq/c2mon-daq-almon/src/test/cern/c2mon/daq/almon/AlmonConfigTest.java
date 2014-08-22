/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.daq.almon;

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

import cern.c2mon.daq.almon.AlmonConfig;

/**
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/dmn-almon-config.xml")
@ActiveProfiles("TEST")
public class AlmonConfigTest {

    Logger LOG = LoggerFactory.getLogger(AlmonConfigTest.class);

    @Resource
    AlmonConfig config;

    @Test
    public void testBasicInterface() {
        assertNotNull(config);
        assertEquals(16, config.getSubcriptionsThreadPoolSize());
        assertEquals(64, config.getMaxPlsLine());
    }

}