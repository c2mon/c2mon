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
package cern.c2mon.shared.rule;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Lets make sure we can read the RULES from a given XML file.
 * @see http://issues.cern.ch/browse/TIM-803
 */
public class FromXmlTest {

  /** XML with rules, extracted from the database */
  private static final String XML_PATH = "rules.xml";
  
  /** Rules contained in the XML FILE */
  private static final int RULE_COUNT = 1339;
  
  @Test
  /**
   * Lets make sure we can read the RULES from the given XML file.
   * This file is extracted from the database.
   */
  public void validateRules()  {

    try {
      Collection<RuleExpression> rules = RuleExpression
          .createExpressionFromDatabaseXML(new ClassPathResource(XML_PATH).getFile().getAbsolutePath());
      
      assertTrue(rules.size() == RULE_COUNT);
      
    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }
}
