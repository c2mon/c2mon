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
package cern.c2mon.server.common.rule;

import org.apache.log4j.or.ObjectRenderer;

/**
 * Log4j renderer class for formatting RuleTags in the log
 * files.
 *
 * @author Mark Brightwell
 * @deprecated Since the migration to SLF4J this class is obsolete 
 */
@Deprecated
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
