/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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
 *****************************************************************************/
package cern.c2mon.shared.client.alarm;

import java.sql.Timestamp;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


/**
 * @author Matthias Braeger
 */
public class AlarmValueImplTest {

  @Test
  public void testValidAlarmValidation() {
    AlarmValue alarm =
        new AlarmValueImpl(12342L, 1, "FaultMember1", "1FaultFamily", "Info1", 1234L,
            new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() - 10), true);

    BeanPropertyBindingResult result = new BeanPropertyBindingResult(alarm, "alarm");
    assertEquals(0, result.getErrorCount());
  }


  @Test
  public void testXMLSerialization() throws Exception {

      AlarmValueImpl av1 =
        new AlarmValueImpl(12342L, 1, "FaultMember1", "1FaultFamily", "Info1", 1234L,
            new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() - 10), true);

      av1.setTagDescription("Looks brown and a bit red.");
      assertTrue(av1.getXml().contains("tagDescription"));

      AlarmValue av2 = AlarmValueImpl.fromXml(av1.toString());

      assertEquals(av1.getId(), av2.getId());
      assertEquals(av1.getFaultFamily(), av2.getFaultFamily());
      assertEquals(av1.getFaultMember(), av2.getFaultMember());
      assertEquals(av1.getFaultCode(), av2.getFaultCode());
      assertEquals(av1.getTimestamp(), av2.getTimestamp());
      assertEquals(av1.getTagId(), av2.getTagId());
  }
}
