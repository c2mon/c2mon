package cern.c2mon.server.common.rule;

import org.apache.log4j.or.ObjectRenderer;

/**
 * Log4j renderer class for formatting RuleTags in the log
 * files.
 * 
 * @author Mark Brightwell
 *
 */
public class RuleTagRenderer implements ObjectRenderer {

  @Override
  public String doRender(Object object) {    
      if (object instanceof RuleTag) {
        RuleTag ruleTag = (RuleTag) object;
        StringBuffer str = new StringBuffer();

        str.append(ruleTag.getId());
        str.append('\t');
        str.append(ruleTag.getName());
        str.append('\t');
        str.append(ruleTag.getTimestamp());
        str.append('\t');
        str.append(ruleTag.getValue());
        if (!ruleTag.isValid()) {
          str.append('\t');
          str.append(ruleTag.getDataTagQuality().getInvalidQualityStates());          
        }
        return str.toString(); 
      } else {        
        return object.toString();
      }
  }

}
