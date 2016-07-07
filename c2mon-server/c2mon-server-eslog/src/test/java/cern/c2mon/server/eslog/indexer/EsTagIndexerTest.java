/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.indexer;

import java.sql.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.connector.Connector;
import cern.c2mon.server.eslog.structure.converter.EsTagLogConverter;
import cern.c2mon.server.eslog.structure.types.tag.EsTag;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;

import static org.mockito.Mockito.when;

/**
 * @author mbraeger
 */
@RunWith(MockitoJUnitRunner.class)
public class EsTagIndexerTest {

  @Mock
  Connector connectorMock;

  @Mock
  private Tag tagC2MON;

  @InjectMocks
  private EsTagLogConverter esLogConverter;

  @InjectMocks
  private EsTagIndexer tagIndexer;

  @Before
  public void before() {
    tagIndexer.setTypePrefix("tag_");
    tagIndexer.setIndexPrefix("c2mon-tag_");
    tagIndexer.setIndexFormat("M");
  }

  @Test
  public void testSendTagToBatch() {
    tagIndexer.sendTagToBatch(getAbstractEsTag(Integer.MAX_VALUE));
  }

  private EsTag getAbstractEsTag(final Object value) {
    long id = 1L;
    String name = "tag";
    long timeStamp = 123456L;
    String valueDesc = "ok";
    String dataType = value.getClass().getName();

    when(tagC2MON.getId()).thenReturn(id);
    when(tagC2MON.getName()).thenReturn(name);
    when(tagC2MON.getDataType()).thenReturn(dataType);
    when(tagC2MON.getCacheTimestamp()).thenReturn(new Timestamp(timeStamp));
    when(tagC2MON.getDataTagQuality()).thenReturn(null);
    when(tagC2MON.getValue()).thenReturn(value);
    when(tagC2MON.getTimestamp()).thenReturn(new Timestamp(1234567));
    when(tagC2MON.getDataTagQuality()).thenReturn(new DataTagQualityImpl());
    when(tagC2MON.getValueDescription()).thenReturn(valueDesc);
    when(tagC2MON.getUnit()).thenReturn("km");

    return esLogConverter.convert(tagC2MON);
  }
}
