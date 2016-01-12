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
package cern.c2mon.notification.impl;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

import cern.c2mon.client.core.tag.ClientDataTagImpl;
import cern.c2mon.notification.Tag;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TransferTagImpl;
import cern.c2mon.shared.common.datatag.DataTagQualityImpl;
import cern.c2mon.shared.common.datatag.TagQualityStatus;
import cern.c2mon.shared.rule.RuleFormatException;

public class TagTest {

    /**
     * 
     * @throws RuleFormatException in case the rule is invalid
     */
    @Test
    public void testMethodhasValueChanged() throws RuleFormatException {
        ClientDataTagImpl before = new ClientDataTagImpl(1L);
        DataTagQualityImpl q = new DataTagQualityImpl();
        q.removeInvalidStatus(TagQualityStatus.UNINITIALISED);
        TransferTagImpl t = new TransferTagImpl(new Long(1L), 1.1d , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-" + 1, null);
        before.update(t);
        
        ClientDataTagImpl after = new ClientDataTagImpl(1L);
        t = new TransferTagImpl(1L, 2.1d , "description", q, TagMode.OPERATIONAL, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "metric tag description", "MetricTag-" + 1, null);
        after.update(t);
        
        assertTrue(Tag.hasValueChanged(before, after));
    }
    
}
