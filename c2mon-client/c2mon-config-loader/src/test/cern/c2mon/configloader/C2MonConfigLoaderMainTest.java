/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.configloader.dao.ConfigLoaderTestDAO;
import cern.c2mon.configloader.requestor.TestReconfigurationRequestor;

/**
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/c2mon-configloader-config.xml")
@ActiveProfiles({ "TEST"/* , "AOP" */})
public class C2MonConfigLoaderMainTest {

    @Autowired
    C2MonConfigLoaderMain main;

    @Autowired
    TestReconfigurationRequestor requestor;

    @Autowired
    ConfigLoaderTestDAO dao;

    @Autowired
    C2MonConfigLoaderConfig conf;

    @Before
    public void setUp() {
        main.init();
    }

    @DirtiesContext
    @Test(timeout = 5000)
    public void testConfigurationsApplication1() throws Exception {

        while (requestor.getReconfigurationRequests().size() < 3) {
            Thread.sleep(50);
        }

        // make sure all were applied
        assertEquals(0, dao.getConfigurationsForLoading().size());
    }

    @DirtiesContext
    @Test(timeout = 5000)
    public void testConfigurationsApplication2() throws Exception {

        Configuration c1 = new Configuration(-1, "test1", "test conf1", "wbuczak",
                System.currentTimeMillis() - 43200000, -1);
        Configuration c2 = new Configuration(-1, "test2", "test conf2", "wbuczak",
                System.currentTimeMillis() - 43200000 - 4000, -1);
        Configuration c3 = new Configuration(-1, "test3", "test conf3", "wbuczak",
                System.currentTimeMillis() - 43200000 - 3000, -1);
        Configuration c4 = new Configuration(-1, "test4", "test conf4", "wbuczak",
                System.currentTimeMillis() - 43200000 - 2000, -1);
        Configuration c5 = new Configuration(-1, "test5", "test conf5", "wbuczak",
                System.currentTimeMillis() - 43200000 - 1000, -1);

        dao.insert(c1);
        Thread.sleep(200);
        dao.insert(c2);
        Thread.sleep(210);
        dao.insert(c3);
        Thread.sleep(300);
        dao.insert(c4);
        Thread.sleep(150);
        dao.insert(c5);

        while (requestor.getReconfigurationRequests().size() < 8) {
            Thread.sleep(50);
        }

        // make sure ALL were applied
        assertEquals(0, dao.getConfigurationsForLoading().size());
    }

}
