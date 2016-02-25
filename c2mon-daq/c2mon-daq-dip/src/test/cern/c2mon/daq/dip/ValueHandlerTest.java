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
package cern.c2mon.daq.dip;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.IEquipmentMessageSender;
import cern.c2mon.shared.common.datatag.ISourceDataTag;
import cern.c2mon.shared.common.datatag.SourceDataQuality;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.datatag.address.DIPHardwareAddress;
import cern.c2mon.shared.common.datatag.address.impl.DIPHardwareAddressImpl;
import cern.dip.DipData;
import cern.dip.DipFactory;
import cern.dip.DipTimestamp;
import cern.dip.mock.MockDipData;
import cern.dip.mock.MockDipFactory;

/**
 * @author Matthias Braeger
 */
public class ValueHandlerTest {

  private ValueHandler handler;
  private DIPController controllerMock = createMock(DIPController.class);
  private IEquipmentMessageSender equipmentMessageSender = createMock(IEquipmentMessageSender.class);

  private static long tagIdCounter = 1L;

  @Before
  public void before() {
    handler = new ValueHandler(controllerMock);

    reset(controllerMock, equipmentMessageSender);
    expect(controllerMock.getEquipmentMessageSender()).andReturn(equipmentMessageSender);
    expect(equipmentMessageSender.sendTagFiltered((ISourceDataTag) EasyMock.anyObject(), EasyMock.anyObject(), EasyMock.anyLong())).andReturn(true);
    replay(controllerMock, equipmentMessageSender);
  }

  @After
  public void after() {
    verify(controllerMock, equipmentMessageSender);
  }

  private void resetMocks() {
    after();
    before();
  }


  @Test
  public void sendBoolean() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Boolean"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(false);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendBooleanArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Boolean_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Boolean_Array", 1));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new boolean[]{false, true});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(1));
  }

  @Test
  public void sendByte() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Byte"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert((byte)0x4f);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendByteArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Byte_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Byte_Array", 1));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new byte[]{(byte)0xe0, 0x4f, (byte)0xd0});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(1));
  }

  @Test
  public void sendDouble() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Double"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(2342.754734d);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendDoubleArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Double_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Double_Array", 1));


    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new double[]{1.2423523452345d, -3.234234d});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(1));
  }

  @Test
  public void sendFloat() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Float"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(1.7f);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendFloatArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Float_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Float_Array", 2));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new float[]{1.7f, -3.5f});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(0));
  }

  @Test
  public void sendInteger() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Integer"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(234);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendIntegerArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Integer_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Integer_Array", 0));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new int[]{0, 1, 2, 3, 4, 5});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(5));
  }

  @Test
  public void sendLong() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Long"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(2542354L);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Long_Array"));

   resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Long_Array", 1));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new long[]{234234L, 353245L, 345345L, 345345L, 6347634L, -983045L});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(5));
  }

  @Test
  public void sendShort() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Short"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert((short) 3);
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendShortArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Short_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("Short_Array", 2));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new short[]{33, 44, 55});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(1));
  }

  @Test
  public void sendString() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("String"));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert("Simple_Type");
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());
  }

  @Test
  public void sendStringArray() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("String_Array"));

    resetMocks();

    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress("String_Array", 2));

    resetMocks();

    DipData dipData = createEmptyDipDataMock();
    dipData.insert(new String[]{"Test", "Test2", "Test3"});
    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress());

    resetMocks();

    handler.parseMessageAndSendValue(dipData, getSourceDataTag(), getHardwareAddress(1));
  }

  @Test
  public void parseUnknownField() throws Exception {
    String unknownFieldName = "Does not exist";
    ISourceDataTag tag = getSourceDataTag();

    reset(controllerMock, equipmentMessageSender);
    expect(controllerMock.getEquipmentMessageSender()).andReturn(equipmentMessageSender);
    equipmentMessageSender.sendInvalidTag(tag,
                                          SourceDataQuality.INCORRECT_NATIVE_ADDRESS,
                                          "Unknown field \"" + unknownFieldName + "\" specified in DIP address.");
    replay(controllerMock, equipmentMessageSender);

    handler.parseMessageAndSendValue(createComplexDipData(), tag, getHardwareAddress(unknownFieldName));
  }

  @Test
  public void sendComplexDipTopicAsJsonString() throws Exception {
    handler.parseMessageAndSendValue(createComplexDipData(), getSourceDataTag(), getHardwareAddress());
  }


  private DipData createEmptyDipDataMock() {
    DipFactory factory = new MockDipFactory();
    MockDipData dipData = (MockDipData) factory.createDipData();
    dipData.setDipTime(new DipTimestamp(1234L));

    return dipData;
  }

  private DipData createComplexDipData() throws Exception {
    DipData dipData = createEmptyDipDataMock();

    dipData.insert("Boolean", false);
    dipData.insert("Boolean_Array", new boolean[]{false, true});
    dipData.insert("Byte", (byte)0x4f);
    dipData.insert("Byte_Array", new byte[]{(byte)0xe0, 0x4f, (byte)0xd0});
    dipData.insert("Double", 1.2423523452345d);
    dipData.insert("Double_Array", new double[]{1.2423523452345d, -3.234234d});
    dipData.insert("Float", 3.226543f);
    dipData.insert("Float_Array", new float[]{3.226543f, 5.23423f, 123.12312f});
    dipData.insert("Integer", 2);
    dipData.insert("Integer_Array", new int[]{0, 1, 2, 3, 4, 5, 6, 7});
    dipData.insert("Long", 333l);
    dipData.insert("Long_Array", new long[]{333l, 666l});
    dipData.insert("Short", (short) 2);
    dipData.insert("Short_Array", new short[]{33, 44, 55});
    dipData.insert("String", "Test");
    dipData.insert("String_Array", new String[]{"Test", "Test2", "Test3"});

    return dipData;
  }

  private DIPHardwareAddress getHardwareAddress(String field, int index) {
    return new DIPHardwareAddressImpl("/dip/test/this/class", field, index);
  }

  private DIPHardwareAddress getHardwareAddress(String field) {
    return new DIPHardwareAddressImpl("/dip/test/this/class", field);
  }

  private DIPHardwareAddress getHardwareAddress() {
    return new DIPHardwareAddressImpl("/dip/test/this/class");
  }

  private DIPHardwareAddress getHardwareAddress(int index) {
    return new DIPHardwareAddressImpl("/dip/test/this/class", index);
  }

  private ISourceDataTag getSourceDataTag() {
    SourceDataTag sdt = new SourceDataTag(tagIdCounter, "Test" + tagIdCounter++, false);
    return sdt;
  }

}
