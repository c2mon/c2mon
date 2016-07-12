/*
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 */

package cern.c2mon.shared.client.serialize;

import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.tag.TransferTagValueImpl;
import cern.c2mon.shared.util.json.GsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static cern.c2mon.shared.client.serializer.TransferTagSerializer.*;
import static cern.c2mon.shared.client.tag.TransferTagValueImplTest.createTagForValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by fritter on 23/03/16.
 */
public class Serializer {

  private static ObjectMapper jacksonMapper = new ObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

  private static Gson gsonMapper = GsonFactory.createGsonBuilder().create();

  static TransferTagValueImpl tag1;
  static TransferTagValueImpl tag2;
  static TransferTagValueImpl tag3;
  static TransferTagValueImpl tag4;
  static TransferTagValueImpl tag5;

  @BeforeClass
  public static void setUpClass(){

    Object complexTagValue = TestUpdateTag.builder()
        .tagId(666L)
        .description("hallo")
        .simulated(false)
        .build();

    tag1 = createTagForValue(complexTagValue);
    tag1.setValueClassName(TestUpdateTag.class.getName());

    tag2 = createTagForValue(111);
    tag2.setValueClassName(Integer.class.getName());

    tag3 = createTagForValue(1.0);
    tag3.setValueClassName(Double.class.getName());

    tag4 = createTagForValue("Hello");
    tag4.setValueClassName(String.class.getName());

    tag5 = createTagForValue(new Integer[]{1,3,3,7});
    tag5.setValueClassName(Integer[].class.getName());
  }

  @Test
  public void serializeWithClass() {

    String jacksonJsonTag1 = toJson(tag1);
    String jacksonJsonTag2 = toJson(tag2);
    String jacksonJsonTag3 = toJson(tag3);
    String jacksonJsonTag4 = toJson(tag4);
    String jacksonJsonTag5 = toJson(tag5);

    TransferTagValueImpl decodesTag1 = fromJson(jacksonJsonTag1, TransferTagValueImpl.class);
    TransferTagValueImpl decodesTag2 = fromJson(jacksonJsonTag2, TransferTagValueImpl.class);
    TransferTagValueImpl decodesTag3 = fromJson(jacksonJsonTag3, TransferTagValueImpl.class);
    TransferTagValueImpl decodesTag4 = fromJson(jacksonJsonTag4, TransferTagValueImpl.class);
    TransferTagValueImpl decodesTag5 = fromJson(jacksonJsonTag5, TransferTagValueImpl.class);

    assertEquals(tag1, decodesTag1);
    assertEquals(tag2, decodesTag2);
    assertEquals(tag3, decodesTag3);
    assertEquals(tag4, decodesTag4);

    assertTrue(Arrays.equals( (Object[])tag5.getValue(), (Object[]) (decodesTag5.getValue())) );
    assertEquals(tag5.getId(), decodesTag5.getId());
    assertEquals(tag5.getAlarms(), decodesTag5.getAlarms());
    assertEquals(tag5.getDaqTimestamp(), decodesTag5.getDaqTimestamp());
    assertEquals(tag5.getDescription(), decodesTag5.getDescription());
    assertEquals(tag5.getDataTagQuality(), decodesTag5.getDataTagQuality());
    assertEquals(tag5.getServerTimestamp(), decodesTag5.getServerTimestamp());
    assertEquals(tag5.getSourceTimestamp(), decodesTag5.getSourceTimestamp());
    assertEquals(tag5.getValueClassName(), decodesTag5.getValueClassName());
    assertEquals(tag5.getMode(), decodesTag5.getMode());
    assertEquals(tag5.getValueDescription(), decodesTag5.getValueDescription());
  }

  @Test
  public void gsonJsonDeserializationOfResponseCollection() {

    Collection<TransferTagValueImpl> response = new ArrayList<>();
    response.add(tag1);
    response.add(tag2);
    response.add(tag3);
    response.add(tag4);

    String gsonJson = gsonMapper.toJson(response);

    // Jackson:
    TypeReference jacksonCollectionType =  new TypeReference<Collection<TransferTagValueImpl>>() { };
    Collection<ClientRequestResult> jacksonRead = fromCollectionJson(gsonJson, jacksonCollectionType);

    assertTrue(response.equals(jacksonRead));
  }

  @Test
  public void jacksonJsonDeserializationOfResponseCollection() {

    try {
      Collection<TransferTagValueImpl> response = new ArrayList<>();
      response.add(tag1);
      response.add(tag2);
      response.add(tag3);
      response.add(tag4);

      String jacksonJson = jacksonMapper.writeValueAsString(response);

      // Jackson:
      TypeReference jacksonCollectionType =  new TypeReference<Collection<TransferTagValueImpl>>() { };
      Collection<ClientRequestResult> jacksonRead = fromCollectionJson(jacksonJson, jacksonCollectionType);

      assertTrue(response.equals(jacksonRead));

    } catch (IOException e) {
      assertTrue(false);
    }
  }
}
