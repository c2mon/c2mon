package cern.c2mon.daq.opcua.connection.common.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.common.impl.DefaultGroupProvider;
import cern.c2mon.daq.opcua.connection.common.impl.ItemDefinition;
import cern.c2mon.daq.opcua.connection.common.impl.SubscriptionGroup;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.common.datatag.DataTagDeadband;
import cern.tim.shared.daq.datatag.SourceDataTag;

public class DefaultGroupProviderTest {
    
    private DefaultGroupProvider<?> provider = 
        new DefaultGroupProvider<ItemDefinition<?>>();
    
    
    @Test
    public void testGetOrCreateGroupSame() {
        SourceDataTag tag = createSourceDataTag(100, 1.1f);
        SubscriptionGroup<?> group = provider.getOrCreateGroup(tag);
        SubscriptionGroup<?> group2 = provider.getOrCreateGroup(tag);
        assertEquals(group, group2);
    }
    
    @Test
    public void testGetOrCreateGroupDifferent() {
        SourceDataTag tag = createSourceDataTag(100, 1.1f);
        SourceDataTag tag2 = createSourceDataTag(101, 1.1f);
        SubscriptionGroup<?> group = provider.getOrCreateGroup(tag);
        SubscriptionGroup<?> group2 = provider.getOrCreateGroup(tag2);
        assertNotSame(group, group2);
    }
    @Test
    public void testGetOrCreateGroupBothDeadbands() {
        SourceDataTag tag = createSourceDataTag(100, 1.1f);
        SubscriptionGroup<?> group = provider.getOrCreateGroup(tag);
        assertEquals(100, group.getTimeDeadband());
        assertTrue(Math.abs(group.getValueDeadband() - 1.1f) < 0.00000000001f);
    }
    
    @Test
    public void testGetOrCreateGroupTimeDeadbandOnly() {
        SourceDataTag tag = createSourceDataTag(100);
        SubscriptionGroup<?> group = provider.getOrCreateGroup(tag);
        assertEquals(100, group.getTimeDeadband());
        assertTrue(0.0f == group.getValueDeadband());
    }
    
    @Test
    public void testGetOrCreateGroupValueDeadbandOnly() {
        SourceDataTag tag = createSourceDataTag(0, 1.1f);
        SubscriptionGroup<?> group = provider.getOrCreateGroup(tag);
        assertEquals(0, group.getTimeDeadband());
        assertTrue(Math.abs(group.getValueDeadband() - 1.1f) < 0.00000000001f);
    }

    private SourceDataTag createSourceDataTag(int timeDeadband, float valueDeaband) {
        SourceDataTag tag = createSourceDataTag(timeDeadband);
        tag.getAddress().setValueDeadbandType(
                DataTagDeadband.DEADBAND_EQUIPMENT_RELATIVE);
        tag.getAddress().setValueDeadband(valueDeaband);
        return tag;
    }

    private SourceDataTag createSourceDataTag(int timeDeadband) {
        DataTagAddress address = new DataTagAddress();
        address.setTimeDeadband(timeDeadband);
        SourceDataTag tag = 
            new SourceDataTag(1L, "asd", false, (short)0, "Boolean", address);
        return tag;
    }
}
