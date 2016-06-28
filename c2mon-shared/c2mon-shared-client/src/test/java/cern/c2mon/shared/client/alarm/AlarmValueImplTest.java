/******************************************************************************
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
 *****************************************************************************/
package cern.c2mon.shared.client.alarm;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.sql.Timestamp;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
//import org.hibernate.validator.HibernateValidator;


/**
 * @author Matthias Braeger
 */
public class AlarmValueImplTest {

  /** Bean validator */
//  private LocalValidatorFactoryBean validator;
  
//  @Before
//  public void setup() {
//    validator = new LocalValidatorFactoryBean();
////    validator.setProviderClass(HibernateValidator.class);
//    validator.afterPropertiesSet();
//    
//    
//  }
  
  @Test
  public void testValidAlarmValidation() {
    AlarmValue alarm =
      new AlarmValueImpl(12342L, 1, "FaultMember1", "1FaultFamily", "Info1",
                         1234L, new Timestamp(System.currentTimeMillis()), true);
    
    BeanPropertyBindingResult result = new BeanPropertyBindingResult(alarm, "alarm");
//    validator.validate(alarm, result);
    assertEquals(0, result.getErrorCount());
  }
  
  @Test
  public void testAlarmValidationWithFutureTimestamp() {    
    AlarmValue alarm = 
      new AlarmValueImpl(12342L, 1, "FaultMember1", "1FaultFamily", "Info1",
                         1234L, new Timestamp(System.currentTimeMillis() + 5000), true);
    
    
//    Set<ConstraintViolation<AlarmValue>> constraintViolations = validator.validate(alarm);
//    assertTrue("Expected validation error not found", constraintViolations.size() == 1);
//    
//    for (ConstraintViolation<AlarmValue> cv : constraintViolations) {
//      String path = cv.getPropertyPath().toString();
//      if ("timestamp".equals(path)) {
//        assertTrue(cv.getConstraintDescriptor().getAnnotation() instanceof Past);
//      }
//      else {
//        fail("Invalid constraint violation with path '" + path + "'");
//      }
//    }
  }
  
  
  @Test
  public void testXMLSerialization() throws Exception {
      
      AlarmValueImpl av1 =                 
          new AlarmValueImpl(12342L, 1, "FaultMember1", "1FaultFamily", "Info1",
                             1234L, new Timestamp(System.currentTimeMillis()), true);
      
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
