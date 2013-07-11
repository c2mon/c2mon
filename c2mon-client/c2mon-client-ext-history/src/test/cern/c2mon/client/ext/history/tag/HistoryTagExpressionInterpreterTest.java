/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 * 
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.tag;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.client.ext.history.common.tag.HistoryTagConfiguration;
import cern.c2mon.client.ext.history.common.tag.HistoryTagParameter;
import cern.c2mon.client.ext.history.common.tag.HistoryTagResultType;
import cern.c2mon.client.ext.history.tag.HistoryTagConfigurationImpl;
import cern.c2mon.client.ext.history.tag.HistoryTagExpressionInterpreter;

/**
 * Tests the class {@link HistoryTagExpressionInterpreter}, and the
 * {@link HistoryTagConfigurationImpl#createExpression()}
 * 
 * @author vdeila
 * 
 */
public class HistoryTagExpressionInterpreterTest {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testInterpreter() throws Exception {

    final HistoryTagConfigurationImpl configuration = new HistoryTagConfigurationImpl();
    configuration.setValue(HistoryTagParameter.TagId, 123456L);
    configuration.setValue(HistoryTagParameter.Days, 3);
    configuration.setValue(HistoryTagParameter.Result, HistoryTagResultType.Values);

    final String expression = configuration.createExpression();

    final HistoryTagConfiguration interpretation = HistoryTagConfigurationImpl.valueOf(expression);

    assertEquals(configuration.getTagId(), interpretation.getTagId());
    assertEquals(configuration.getTotalMilliseconds(), interpretation.getTotalMilliseconds());
    assertEquals(configuration.getValue(HistoryTagParameter.Result), interpretation.getValue(HistoryTagParameter.Result));
  }

  @Test
  public void testInterpreter2() throws Exception {
    final HistoryTagConfigurationImpl configuration = new HistoryTagConfigurationImpl();
    configuration.setValue(HistoryTagParameter.Result, HistoryTagResultType.Values);
    configuration.setValue(HistoryTagParameter.TagId, 123457L);
    configuration.setValue(HistoryTagParameter.Days, 4);
    configuration.setValue(HistoryTagParameter.Hours, 12);
    configuration.setValue(HistoryTagParameter.Records, 100);
    configuration.setValue(HistoryTagParameter.InitialRecord, true);
    configuration.setValue(HistoryTagParameter.Supervision, true);

    final String expression = configuration.createExpression();

    final HistoryTagConfiguration interpretation = HistoryTagConfigurationImpl.valueOf(expression);

    assertEquals(configuration.getValue(HistoryTagParameter.Result), interpretation.getValue(HistoryTagParameter.Result));
    assertEquals(configuration.getTagId(), interpretation.getTagId());
    assertEquals(configuration.getTotalMilliseconds(), interpretation.getTotalMilliseconds());
    assertEquals(configuration.getRecords(), interpretation.getRecords());
    assertEquals(configuration.isInitialRecord(), interpretation.isInitialRecord());
    assertEquals(configuration.isSupervision(), interpretation.isSupervision());
  }
  
  @Test
  public void testInterpreter3() throws Exception {

    final HistoryTagConfigurationImpl configuration = new HistoryTagConfigurationImpl();
    configuration.setValue(HistoryTagParameter.Result, HistoryTagResultType.Values);
    configuration.setValue(HistoryTagParameter.TagId, 123456L);
    configuration.setValue(HistoryTagParameter.Days, 3);
    

    final String expression = configuration.createExpression().toLowerCase();

    final HistoryTagConfiguration interpretation = HistoryTagConfigurationImpl.valueOf(expression);

    assertEquals(configuration.getValue(HistoryTagParameter.Result), interpretation.getValue(HistoryTagParameter.Result));
    assertEquals(configuration.getTagId(), interpretation.getTagId());
    assertEquals(configuration.getTotalMilliseconds(), interpretation.getTotalMilliseconds());
  }
  
  @Test
  public void testInterpreterConditional() throws Exception {

    final HistoryTagConfigurationImpl configuration = new HistoryTagConfigurationImpl();
    configuration.setValue(HistoryTagParameter.Result, HistoryTagResultType.Conditional);
    configuration.setValue(HistoryTagParameter.TagId, 123456L);
    configuration.setValue(HistoryTagParameter.Days, 3);
    configuration.setValue(HistoryTagParameter.LoadingValue, "Please wait while loading.");
    configuration.setValue(HistoryTagParameter.FailedValue, "Failed to load the chart!");
    configuration.setValue(HistoryTagParameter.ActiveValue, "The last 3 days");

    final String expression = configuration.createExpression();

    final HistoryTagConfiguration interpretation = HistoryTagConfigurationImpl.valueOf(expression);

    assertEquals(configuration.getValue(HistoryTagParameter.Result), interpretation.getValue(HistoryTagParameter.Result));
    assertEquals(configuration.getTagId(), interpretation.getTagId());
    assertEquals(configuration.getTotalMilliseconds(), interpretation.getTotalMilliseconds());
    assertEquals(configuration.getValue(HistoryTagParameter.LoadingValue), interpretation.getValue(HistoryTagParameter.LoadingValue));
    assertEquals(configuration.getValue(HistoryTagParameter.FailedValue), interpretation.getValue(HistoryTagParameter.FailedValue));
    assertEquals(configuration.getValue(HistoryTagParameter.ActiveValue), interpretation.getValue(HistoryTagParameter.ActiveValue));
  }

}
