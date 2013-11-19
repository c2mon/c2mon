package cern.c2mon.shared.rule;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

/**
 * Lets make sure we can read the RULES from a given XML file.
 * @see http://issues.cern.ch/browse/TIM-803
 */
public class FromXmlTest {

  /** XML with rules, extracted from the database */
  private static final String XML_PATH = "src/test/cern/c2mon/shared/rule/rules.xml";
  
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
          .createExpressionFromDatabaseXML(XML_PATH);
      
      assertTrue(rules.size() == RULE_COUNT);
      
    } catch (Exception e) {
      assertTrue(false);
    }
    assertTrue(true);
  }
}
