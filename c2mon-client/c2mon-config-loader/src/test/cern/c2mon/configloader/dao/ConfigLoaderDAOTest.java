/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.configloader.C2MonConfigLoaderConfig;
import cern.c2mon.configloader.Configuration;

/**
 * @author wbuczak
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/resources/c2mon-configloader-config.xml")
@ActiveProfiles({ "TEST", "AOP" })
public class ConfigLoaderDAOTest {

    Logger LOG = LoggerFactory.getLogger(ConfigLoaderDAOTest.class);

    @Resource
    C2MonConfigLoaderConfig conf;

    @Resource
    ConfigLoaderTestDAO dao;

    @Autowired
    DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    public void testGetConfigurationsForLoading() {
        assertNotNull(dao);

        List<Configuration> configs = dao.getConfigurationsForLoading();

        assertNotNull(configs);
        assertEquals(3, configs.size());

        Configuration conf1 = configs.get(0);

        assertNotNull(conf1);
        assertEquals(1000L, conf1.getId());
        assertEquals("Remove Computer", conf1.getName());
        assertEquals("cfp-363-alinrf", conf1.getDescription());
        assertEquals("CCONFSUP", conf1.getAuthor());
        assertEquals(Configuration.TIMESTAMP_NOT_SET, conf1.getApplyTimestamp());

    }

    @Test
    public void testUpdating() {
        dao.update(1000, "TestUser1");

        String userName = jdbcTemplate.queryForObject("select applied_by from c2mon_config where configid=1000",
                String.class);
        assertEquals("TestUser1", userName);

        // test updating applied timestamp
        assertEquals(3, dao.getConfigurationsForLoading().size());
        dao.setAppliedFlag(1000);
        assertEquals(2, dao.getConfigurationsForLoading().size());
    }

}