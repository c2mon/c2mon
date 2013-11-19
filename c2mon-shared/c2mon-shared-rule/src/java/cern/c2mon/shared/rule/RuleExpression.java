package cern.c2mon.shared.rule;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import cern.c2mon.shared.common.type.TypeConverter;

public abstract class RuleExpression 
    implements IRuleExpression {

  private static final long serialVersionUID = -8053889874595191829L;

  /**
   * Text of the rule expression (as defined by the user)
   */
  protected final String expression;

  /**
   * Rules extracted from the database use the following tag in the xml file 
   * to separate each rule from each other.
   */
  private static final String RULE_DATABASE_XML_TAG = "TAGRULE";

  /**
   * Default constructor
   * 
   * @param pExpression The rule expression string
   */
  public RuleExpression(final String pExpression) {
    this.expression = pExpression.trim();
  }

  /**
   * Clone implementation.
   */
  public Object clone() {
    try {
      RuleExpression ruleExpression = (RuleExpression) super.clone();
      return ruleExpression;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new RuntimeException("Exception caught when cloning a RuleExpression object - this should not happen!!");
    }
  }

  /**
   * The method evaluates the rule for the given input values of the tags. 
   * The result is then casted into the given result type class
   * 
   * @param pInputParams Map of value objects related to the input tag ids
   * @param resultType The result type class to which the rule result shall be casted
   * @return The casted rule result for the given input values
   * @throws RuleEvaluationException In case of errors during the rule evaluation
   */
  public final <T> T evaluate(final Map<Long, Object> pInputParams, Class<T> resultType)
      throws RuleEvaluationException {
    
    try {
      return TypeConverter.castToType(evaluate(pInputParams), resultType);
    } catch (ClassCastException ce) {
      throw new RuleEvaluationException("Rule result cannot be converted to " + resultType.getName());
    }
  }
  
  /**
   * Calculates a value for a rule even if it is marked as Invalid
   * (this can be possible if a value is received for that Invalid tag).
   * @return The casted rule result for the given input values.
   * 
   * @param pInputParams Map of value objects related to the input tag ids
   * @param resultType The result type class to which the rule result shall be casted
   */
  public final <T> T forceEvaluate(final Map<Long, Object> pInputParams, Class<T> resultType) {
    
    return TypeConverter.castToType(forceEvaluate(pInputParams), resultType);
  }

  /**
   * @return List of input tag id's
   */
  public abstract Set<Long> getInputTagIds();

  /**
   * Static method that creates a {@link RuleExpression} object due to the given rule string. The following two class can be returned: <li>
   * {@link SimpleRuleExpression}: In case of a rule without conditions <li>
   * {@link ConditionedRuleExpression}: In case of a rule with conditions
   * 
   * @param pExpression the rule as string representation
   * @return An instance of a {@link RuleExpression}
   * @throws RuleFormatException In case of errors in parsing the rule expression string
   */
  public static RuleExpression createExpression(final String pExpression) throws RuleFormatException {
    if (pExpression != null) {
      if (pExpression.indexOf(",") == -1) {
        // simple rule --> the whole expression is the rule
        if (MultipleReturnValueRuleExpression
            .isMultipleReturnValueExpression(pExpression)) {
          return new MultipleReturnValueRuleExpression(pExpression);
        }
        else {
          return new SimpleRuleExpression(pExpression);
        }
      } else {
        return new ConditionedRuleExpression(pExpression);
      }
    } else {
      throw new RuleFormatException("Rule expression cannot be null.");
    }

  }

  /**
   * @return A Collection of Rules, created from the given XML.
   * 
   * @param XMLpath the path where the XML is stored
   * The XML should follow the format below:
   * (default XML format for Benthic pl / sql editor)
   * 
   * <?xml version="1.0" encoding="ISO-8859-1" ?> 
   * <ROWSET name="Query2">
   * <ROW> <TAGRULE><![CDATA[(#141324 < 10)[2],true[3]]]></TAGRULE></ROW>
   * <ROW> <TAGRULE><![CDATA[(#51083 = false) & (#51090 = false)[0],true[3]]]></TAGRULE></ROW> 
   * </ROWSET>
   */
  public static Collection<RuleExpression> createExpressionFromDatabaseXML(final String XMLpath)
      throws RuleFormatException, SAXException, IOException,
      ParserConfigurationException {

    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

    DocumentBuilder db = dbf.newDocumentBuilder();
    Document dom = db.parse(XMLpath);

    Element doc = dom.getDocumentElement();

    Collection<RuleExpression> ruleStrings = new ArrayList<RuleExpression>();

    NodeList nl;
    nl = doc.getElementsByTagName(RULE_DATABASE_XML_TAG);
    final int size = nl.getLength();
    int currentCount = 0;

    while (currentCount < size) {
      String ruleString = null;

      if (nl.getLength() > 0 && nl.item(currentCount).hasChildNodes()) {
        ruleString = nl.item(currentCount).getFirstChild().getNodeValue();
      }
      RuleExpression ruleExpression = RuleExpression.createExpression(ruleString);
      ruleStrings.add(ruleExpression);
      currentCount++;
    }
    return ruleStrings;
  }

  @Override
  public String toString() {
    
    StringBuffer str = new StringBuffer();
    if (this.expression != null) {
      str.append(this.expression.replace("\n", ""));
    }
    return str.toString();
  }
  
  public String toXml() {

    StringBuffer str = new StringBuffer();
    str.append("<RuleExpression>");
    str.append(this.expression);
    str.append("</RuleExpression>\n");
    return str.toString();
  }

  // TODO: Turn into JUnit tests!
  public static void main(String[] args) {
    try {
      Hashtable<Long, Object> tags = new Hashtable<Long, Object>();
      RuleExpression exp = RuleExpression.createExpression("#1 = #2 [5], true [2]");
      System.out.println("input tags needed: " + exp.getInputTagIds());
      tags.put(new Long(1), new Integer(5));
      tags.put(new Long(2), new Float(5));
      Object result = exp.evaluate(tags, Float.class);
      System.out.println("result     : " + result);
      System.out.println("result type: " + result.getClass().getName());

      /*
       * 
       * System.out.println("creating rule"); RuleExpression exp = RuleExpression.createExpression("2 = 2"); System.out.println("evaluating rule: " +
       * exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp =
       * RuleExpression.createExpression("2 != 2"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable())); System.out.println("creating rule"); exp = RuleExpression.createExpression("(2 = 2)"); System.out.println("evaluating rule: " +
       * exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp =
       * RuleExpression.createExpression("(2 != 2)"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable())); System.out.println("creating rule"); exp = RuleExpression.createExpression("!(2 = 2)"); System.out.println("evaluating rule: " +
       * exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp =
       * RuleExpression.createExpression("!(2 != 2)"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable()));
       * 
       * System.out.println("creating rule"); exp = RuleExpression.createExpression("!(2 = 2) & true"); System.out.println("evaluating rule: " +
       * exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp =
       * RuleExpression.createExpression("!(2 = 2) | false"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable())); System.out.println("creating rule"); exp = RuleExpression.createExpression("(2 = 2) | true"); System.out.println("evaluating rule: " +
       * exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp =
       * RuleExpression.createExpression("(2 = 2) | false"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable())); exp = RuleExpression.createExpression("(2 = 2) | true"); System.out.println("evaluating rule: " + exp.getExpression());
       * System.out.println(exp.evaluate(new Hashtable())); System.out.println("creating rule"); exp = RuleExpression.createExpression("(2 = 2) | !(2=2)");
       * System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new Hashtable())); exp =
       * RuleExpression.createExpression("(2 = 2) | !(2=2)"); System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new
       * Hashtable()));
       * 
       * exp = RuleExpression.createExpression( "(!(1 != 2) & (18000 > 17000)) & (!(3 = 2) & (19000 > 17000) & (2 = 2) & (18000 > 17000))");
       * System.out.println("evaluating rule: " + exp.getExpression()); System.out.println(exp.evaluate(new Hashtable()));
       */
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * @return Get the text of the expression (as defined by the user)
   */
  public String getExpression() {
    return this.expression;
  }

}