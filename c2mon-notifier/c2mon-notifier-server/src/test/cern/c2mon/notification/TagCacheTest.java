/*
 * $Id $
 *
 * $Date$
 * $Revision$
 * $Author$
 *
 * Copyright CERN ${year}, All Rights Reserved.
 */
package cern.c2mon.notification;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Test;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.notification.Tag;
import cern.c2mon.notification.impl.TagCache;
import cern.tim.shared.common.datatag.DataTagQuality;

public class TagCacheTest {

    public class MyCache extends TagCache {
        @Override
        public Tag metricTagResolver2(Long tagID, ConcurrentHashMap<Long, Tag> overallList) {

            // one child with a metric
            Tag rule1 = new Tag(1L, true);
            Tag rule2 = new Tag(2L, true);
            Tag metric = new Tag(3L, false);
            Tag metric2 = new Tag(4L, false);
            rule2.addChildTag(metric);
            rule2.addChildTag(metric2);
            rule1.addChildTag(rule2);
            
            overallList.put(rule1.getId(), rule1);
            overallList.put(rule2.getId(), rule2);
            overallList.put(metric.getId(), metric);
            overallList.put(metric2.getId(), metric2);
            
            return overallList.get(tagID);
        }
    }
    
    /**
     * 
     */
    @Test 
    public void testUpdateCache() {
        IMocksControl mockControl = EasyMock.createControl();
        ClientDataTagValue metricUpdate3 = mockControl.createMock(ClientDataTagValue.class);
        EasyMock.expect(metricUpdate3.getId()).andReturn(new Long(3)).anyTimes();
        EasyMock.expect(metricUpdate3.isRuleResult()).andReturn(false).anyTimes();
        EasyMock.expect(metricUpdate3.getValue()).andReturn(5.5).anyTimes();
        EasyMock.expect(metricUpdate3.getName()).andReturn("Metric-3").anyTimes();
        EasyMock.expect(metricUpdate3.getDescription()).andReturn("test-description").anyTimes();
        EasyMock.expect(metricUpdate3.getValueDescription()).andReturn("value-description").anyTimes();
        EasyMock.expect(metricUpdate3.getServerTimestamp()).andReturn(new Timestamp(System.currentTimeMillis())).anyTimes();
        EasyMock.expect(metricUpdate3.getUnit()).andReturn("double").anyTimes();
                
        
        DataTagQuality dtq3 = mockControl.createMock(DataTagQuality.class);
        EasyMock.expect(dtq3.isExistingTag()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.isValid()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.isAccessible()).andReturn(true).anyTimes();
        EasyMock.expect(dtq3.getDescription()).andReturn("Cool").anyTimes();
        EasyMock.expect(metricUpdate3.getDataTagQuality()).andReturn(dtq3).anyTimes();
        EasyMock.replay(metricUpdate3);
        
        MyCache cache = new MyCache();
        HashSet<Long> list = new HashSet<Long>();
        list.add(1L);
        cache.resolveSubTags(list);
        
        System.out.println(cache);
        
        cache.get(3L).update(metricUpdate3);
        
        System.out.println(cache);
    }
}
