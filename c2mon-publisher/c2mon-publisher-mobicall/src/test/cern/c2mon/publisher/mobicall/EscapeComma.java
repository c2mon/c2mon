/**
 * Copyright (c) 2015 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.publisher.mobicall;

import static org.junit.Assert.*;

import org.junit.Test;

public class EscapeComma {

    private static String s1 = "Hello##comma## world!##comma## cont'd";
    private static String s2 = "Hello, world!, cont'd";
    
    @Test
    public void test() {
        assertEquals(s1.replaceAll("##comma##", ","), s2);
        assertEquals(s2.replaceAll(",", "##comma##"), s1);
    }

}
