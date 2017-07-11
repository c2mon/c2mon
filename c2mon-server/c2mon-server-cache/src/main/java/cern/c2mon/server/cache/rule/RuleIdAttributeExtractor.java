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
package cern.c2mon.server.cache.rule;

import cern.c2mon.server.common.rule.RuleTagCacheObject;
import java.util.stream.Collectors;
import net.sf.ehcache.Element;
import net.sf.ehcache.search.attribute.AttributeExtractor;
import net.sf.ehcache.search.attribute.AttributeExtractorException;

/**
 * Used by EHCache to extract Tag IDs from rules, for searching.
 *
 * @author jhamilto
 */
public class RuleIdAttributeExtractor implements AttributeExtractor {

    /**
     * Get the Tag IDs used by the rule as a string.
     * 
     * @param elmnt
     * @param string
     * 
     * @return concatenated string of RuleIDs separated by hashes e.g. #1234#5678#
     * 
     * @throws AttributeExtractorException 
     */
    @Override
    public Object attributeFor(Element elmnt, String string) throws AttributeExtractorException {
        try {
            RuleTagCacheObject tag = (RuleTagCacheObject)elmnt.getObjectValue();
            return tag.getCopyRuleInputTagIds().stream()
                .map(i -> i.toString())
                .collect(Collectors.joining("#", "#", "#"));
        } catch (Exception e) {
            throw new AttributeExtractorException(e);
        }
    }    
}
