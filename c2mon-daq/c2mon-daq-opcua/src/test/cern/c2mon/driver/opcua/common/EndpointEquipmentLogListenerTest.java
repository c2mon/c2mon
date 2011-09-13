package cern.c2mon.driver.opcua.common;

import static org.easymock.classextension.EasyMock.*;

import org.easymock.classextension.ConstructorArgs;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.driver.opcua.EndpointEquipmentLogListener;
import cern.tim.driver.common.EquipmentLogger;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTag;

public class EndpointEquipmentLogListenerTest {
    
    private EquipmentLogger logger;
    
    private EndpointEquipmentLogListener listener;
        
    
    @Before
    public void setUp() throws SecurityException, NoSuchMethodException {
        logger = createMock(EquipmentLogger.class,
                new ConstructorArgs(
                        EquipmentLogger.class.getConstructor(
                                String.class, String.class, String.class),
                                "asd", "asd", "asd"),
                EquipmentLogger.class.getMethod("isDebugEnabled"),
                EquipmentLogger.class.getMethod("debug", Object.class));
        listener = new EndpointEquipmentLogListener(logger);
    }
    
    @Test
    public void testOnNewTagValue() {
        ISourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        Object value = "";
        long timestamp = 100L;
        
        expect(logger.isDebugEnabled()).andReturn(true);
        logger.debug("New tag value (ID: '" + dataTag.getId() + "',"
                + " Value: '" + value + "', Timestamp: '" + timestamp + "').");
        
        replay(logger);
        listener.onNewTagValue(dataTag, timestamp, value);
        verify(logger);
    }
    
    @Test
    public void testOnSubscriptionException() {
        Throwable cause = new Throwable();
        
        logger.error("Exception in OPC subscription.", cause);
        
        replay(logger);
        listener.onSubscriptionException(cause);
        verify(logger);
    }
    
    @Test
    public void testOnInvalidTagException() {
        Throwable cause = new Throwable();
        ISourceDataTag dataTag = new SourceDataTag(1L, "asd", false);
        
        logger.warn("Tag with id '" + dataTag.getId() + "' caused exception. "
                + "Check configuration.", cause);
        
        replay(logger);
        listener.onTagInvalidException(dataTag, cause);
        verify(logger);
    }

}
