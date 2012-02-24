package cern.c2mon.driver.bic;

import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import org.easymock.EasyMock;
import org.junit.Test;

import cern.tim.driver.test.GenericMessageHandlerTst;
import cern.tim.driver.test.SourceDataTagValueCapture;
import cern.tim.driver.test.UseConf;
import cern.tim.driver.test.UseHandler;

@UseHandler(BICMessageHandler.class)
public class BicMessageHandlerTest extends GenericMessageHandlerTst {

    @Override
    protected void afterTest() throws Exception {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected void beforeTest() throws Exception {
        // TODO Auto-generated method stub
        
    }        
    
    @Test
    @UseConf("e_bic_test1.xml")
    public void subscription_Test1() throws Exception {

        messageSender.sendCommfaultTag(107211, true);
        expectLastCall().once();

        SourceDataTagValueCapture sdtv = new SourceDataTagValueCapture();

        messageSender.addValue(EasyMock.capture(sdtv));

        expectLastCall().times(6);

        replay(messageSender);        
        
        
        assertTrue(true);
    }
    
    @Test
    @UseConf("e_bic_test2.xml")
    public void subscription_Test2() throws Exception {
        
        assertTrue(true);
    }

   
}
