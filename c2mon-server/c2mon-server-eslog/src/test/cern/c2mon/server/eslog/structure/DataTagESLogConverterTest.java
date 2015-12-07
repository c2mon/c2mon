package cern.c2mon.server.eslog.structure;

import cern.c2mon.server.cache.EquipmentCache;
import cern.c2mon.server.cache.ProcessCache;
import cern.c2mon.server.cache.SubEquipmentCache;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.equipment.EquipmentCacheObject;
import cern.c2mon.server.common.process.ProcessCacheObject;
import cern.c2mon.server.common.subequipment.SubEquipmentCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.structure.types.TagES;
import cern.c2mon.server.test.CacheObjectCreation;
import cern.c2mon.shared.common.datatag.DataTagQuality;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Checks on the fields of data appened/set to TagES.
 * @author Alban Marguet.
 */
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class DataTagESLogConverterTest {

    /**
     * The class under test.
     */
    @InjectMocks
    DataTagESLogConverter esLogConverter;

    @Mock
    ProcessCache processCache;

    @Mock
    EquipmentCache equipmentCache;

    @Mock
    SubEquipmentCache subEquipmentCache;

    @Mock
    DataTagQuality dataTagQuality;

    @Mock
    DataTagCacheObject tag;

    @Mock
    Tag tagC2MON;

    @Mock
    TagES tagES;

    @Test
    public void testGetProcessName() {
        ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
        String expected = "process1";
        process.setName(expected);
        when(processCache.get(1L)).thenReturn(process);
        assertEquals(expected, processCache.get(1L).getName());
    }

    @Test
    public void testGetEquipmentName() {
        EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
        String expected = "equipment1";
        equipment.setName(expected);
        when(equipmentCache.get(1L)).thenReturn(equipment);
        assertEquals(expected, equipmentCache.get(1L).getName());
    }

    @Test
    public void testGetSubEquipmentName() {
        SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
        String expected = "subEquipment1";
        subEquipment.setName(expected);
        when(subEquipmentCache.get(1L)).thenReturn(subEquipment);
        assertEquals(expected, subEquipmentCache.get(1L).getName());
    }

    @Test
    public void generalTestGetTagMetadataProcess() {
        ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
        process.setName("process");
        EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
        equipment.setName("equipment");
        equipment.setProcessId(process.getId());
        SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
        subEquipment.setName("subEquipment");
        subEquipment.setParentId(equipment.getId());
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Process", "process");
        expected.put("Equipment", "equipment");
        expected.put("SubEquipment", "subEquipment");

        when(processCache.get(1L)).thenReturn(process);
        when(equipmentCache.get(1L)).thenReturn(equipment);
        when(subEquipmentCache.get(1L)).thenReturn(subEquipment);

        assertEquals(processCache.get(1L), process);
        assertEquals(equipmentCache.get(1L), equipment);
        assertEquals(subEquipmentCache.get(1L), subEquipment);

        when(tag.getSubEquipmentIds()).thenReturn(Collections.singleton(subEquipment.getId()));
        assertEquals(subEquipmentCache.get(1L).getParentId(), equipment.getId());
        assertEquals(equipmentCache.get(1L).getProcessId(), process.getId());
    }

    @Test
    public void testGetTagMetadataProcessWithSubEquipment() {
        ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
        process.setName("process");
        EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
        equipment.setName("equipment");
        equipment.setProcessId(process.getId());
        SubEquipmentCacheObject subEquipment = CacheObjectCreation.createTestSubEquipment();
        subEquipment.setName("subEquipment");
        subEquipment.setParentId(equipment.getId());
        HashMap<String, String> expected = new HashMap<>();
        expected.put("Process", "process");
        expected.put("Equipment", "equipment");
        expected.put("SubEquipment", "subEquipment");

        tag.setProcessId(process.getId());
        when(processCache.get(process.getId())).thenReturn(process);
        tag.setEquipmentId(equipment.getId());
        when(equipmentCache.get(equipment.getId())).thenReturn(equipment);
        tag.setSubEquipmentId(subEquipment.getId());
        when(subEquipmentCache.get(subEquipment.getId())).thenReturn(subEquipment);
        when(tag.getSubEquipmentIds()).thenReturn(Collections.singleton(subEquipment.getId()));

        Map <String, String> result = esLogConverter.getTagMetadataProcess(tag);
        assertEquals(expected, result);
    }

    @Test
    public void testGetTagMetadataProcessWithEquipment() {
        ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
        process.setName("process");
        EquipmentCacheObject equipment = CacheObjectCreation.createTestEquipment();
        equipment.setName("equipment");
        equipment.setProcessId(process.getId());

        HashMap<String, String> expected = new HashMap<>();
        expected.put("Process", "process");
        expected.put("Equipment", "equipment");

        tag.setProcessId(process.getId());
        when(processCache.get(process.getId())).thenReturn(process);
        tag.setEquipmentId(equipment.getId());
        when(equipmentCache.get(equipment.getId())).thenReturn(equipment);
        when(tag.getEquipmentIds()).thenReturn(Collections.singleton(equipment.getId()));

        Map <String, String> result = esLogConverter.getTagMetadataProcess(tag);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMetadataProcessWithProcess() {
        ProcessCacheObject process = CacheObjectCreation.createTestProcess1();
        process.setName("process");

        HashMap<String, String> expected = new HashMap<>();
        expected.put("Process", "process");

        tag.setProcessId(process.getId());
        when(processCache.get(process.getId())).thenReturn(process);
        when(tag.getProcessIds()).thenReturn(Collections.singleton(process.getId()));

        Map <String, String> result = esLogConverter.getTagMetadataProcess(tag);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMetadataProcessWithNull() {
        HashMap<String, String> expected = new HashMap<>();

        Map <String, String> result = esLogConverter.getTagMetadataProcess(tag);
        assertEquals(expected, result);
    }

    @Test
    public void testGetTagMetadataWithNothing() {
        HashMap<String, String> expected = new HashMap<>();
        DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
        tag.setEquipmentId(null);
        tag.setSubEquipmentId(null);
        tag.setProcessId(null);
        assertEquals(expected, esLogConverter.getTagMetadataProcess(tag));
    }

    @Test
    public void testConvertToTagES() throws IOException {
        when(tagC2MON.getId()).thenReturn(1L);
        when(tagC2MON.getName()).thenReturn("tag");
        when(tagC2MON.getDataType()).thenReturn("boolean");
        when(tagC2MON.getCacheTimestamp()).thenReturn(new Timestamp(123456L));
        when(tagC2MON.getDataTagQuality()).thenReturn(null);
        when(tagC2MON.getValue()).thenReturn(true);
        when(tagC2MON.getValueDescription()).thenReturn("ok");

        esLogConverter.convertToTagES(tagC2MON);
        verify(tagES).setMetadataProcess(new HashMap<String, String>());
        verify(tagES).setTagId(1L);
        verify(tagES).setTagName("tag");
        verify(tagES).setDataType("boolean");
        verify(tagES).setTagServerTime(123456L);
        verify(tagES).setTagStatus(0);
        verify(tagES).setTagValue(true);
        verify(tagES).setTagValueDesc("ok");

        when(tagC2MON.getValue()).thenReturn("string");
        esLogConverter.convertToTagES(tagC2MON);
        verify(tagES).setTagValue("string");

        when(tagC2MON.getValue()).thenReturn(123456789);
        esLogConverter.convertToTagES(tagC2MON);
        verify(tagES).setTagValue(123456789);
    }
}
