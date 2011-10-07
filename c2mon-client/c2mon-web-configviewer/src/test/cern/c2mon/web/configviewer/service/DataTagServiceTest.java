package cern.c2mon.web.configviewer.service;


import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import org.springframework.util.Assert;

import cern.c2mon.web.configviewer.model.DataTagInfo;

@ContextConfiguration(locations = {"test-context.xml"} )
@RunWith(SpringJUnit4ClassRunner.class)
public class DataTagServiceTest {

    @Autowired
    DataTagService dtService;
    
    @Test
    public void DataTagInfoNotEmpty() {
        DataTagInfo info = dtService.getDataTagInfo("137242");
        Assert.notEmpty(info.getConfig());
        Assert.notEmpty(info.getValue());
    }
    
    
}
