/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.rdaAlarms;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Ensure that the "last period" and "total" counters of the VCM objects are correct,
 * and check that a change in speed provides the expected results.
 * 
 * The VCM delivers two values, one for the total number observed since its creation,
 * one for the variation over the last amount of time as defined by setVcmSeconds. This
 * allows to create metrics with simple rules to ensure that a process is not blocked.
 * 
 * @author mbuttner
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class TestVCM extends TestBaseClass {

    private VCM vcm;
    
    @Test
    public void testVCM() throws Exception {
        vcm = new VCM();
        vcm.setVcmSeconds(5);
                
        check(1, 6, 5, 5);      // after 6 seconds, with VCM 5 seconds, we should one increment in total and value
        check(1, 2, 5, 5);      // 2 seconds later, nothing should have changed
        check(2, 4, 10, 15);    // another 4 seconds later (now 12), we should have a second increment visible
        
        // change speed and check that this is successfully done
        vcm.setVcmSeconds(10);
        check(2, 5, 10, 15);  // no change because the VCM was slowed down
        check(3, 6, 10, 25);  // after update we should see two additional increments
    }

    private void check(int exec, int sleepSecs, long expectedValue, long expectedTotal) throws Exception {
        getLogger().info("testVCM().check({},{})", exec, sleepSecs);
        for (int i = 0 ; i < 5; i++) {
            vcm.increment();
        }        
        
        Thread.sleep(sleepSecs * 1000);
        getLogger().info("testVCM().check -> {} / {}", vcm.getValue(), vcm.getTotal());
        assertEquals(expectedValue, vcm.getValue());
        assertEquals(expectedTotal, vcm.getTotal());
        
    }
}
