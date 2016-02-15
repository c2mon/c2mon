package cern.c2mon.shared.common.datatag.address.impl;
/******************************************************************************
 * Copyright (C) 2010- CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.shared.common.datatag.DataTagAddress;
import cern.c2mon.shared.util.parser.SimpleXMLParser;
import org.junit.Test;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * @author Franz Ritter
 */
public class DataTagAddressTest {

  @Test
  public void parseBasicHardwareAddressToXml(){
    // initialize test data:
    Map<String,String> testData = new HashMap<>();
    testData.put("key1", "1");
    testData.put("key2", "test");
    testData.put("key3", "true");

    DataTagAddress testAddress= new DataTagAddress(testData);

    String compareString = "      <DataTagAddress>\n" +
        "            <address-parameters class=\"class java.util.HashMap\"><entry key=\"key1\">1</entry><entry key=\"key2\">test</entry><entry key=\"key3\">true</entry></address-parameters>\n" +
        "        <priority>2</priority>\n" +
        "        <guaranteed-delivery>false</guaranteed-delivery>\n" +
        "      </DataTagAddress>\n";

    // test run:
    String resultXml = testAddress.toConfigXML();

    // check results:
    assertEquals(resultXml, compareString);

  }

  @Test
  public void parseXmlToBasicHardwareAddress(){
    // initialize test data:

    String testString = "      <DataTagAddress>\n" +
        "            <address-parameters class=\"class java.util.HashMap\"><entry key=\"key1\">1</entry><entry key=\"key2\">test</entry><entry key=\"key3\">true</entry></address-parameters>\n" +
        "        <priority>2</priority>\n" +
        "        <guaranteed-delivery>false</guaranteed-delivery>\n" +
        "      </DataTagAddress>\n";

    try {
      Element testElement;
      testElement = (Element) new SimpleXMLParser().parse(testString).getFirstChild();
      DataTagAddress resultAddress = DataTagAddress.fromConfigXML(testElement);

      Map<String,String> compareData = new HashMap<>();
      compareData.put("key1", "1");
      compareData.put("key2", "test");
      compareData.put("key3", "true");

      for(Map.Entry<String, String> resultEntry : resultAddress.getAddressParameters().entrySet()){

        // check keys
        assertTrue(compareData.containsKey(resultEntry.getKey()));

        // check values
        assertEquals(resultEntry.getValue(), compareData.get(resultEntry.getKey()));
      }

    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
  }

}
