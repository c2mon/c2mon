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
package cern.c2mon.shared.client.request;

import static org.junit.Assert.*;

import cern.c2mon.shared.client.request.ClientRequestReport;
import org.junit.Test;

/**
 * Unit test of ClientRequestReport.
 * 
 * @author Mark Brightwell
 *
 */
public class ClientRequestReportTest {

  /**
   * Implementation of abstract class for use in tests.
   * @author Mark Brightwell
   *
   */
  private class DummyClientRequestReport extends ClientRequestReport {

    public DummyClientRequestReport() {
      super();      
    }

    public DummyClientRequestReport(boolean pExecutedSuccessfully, String pErrorMessage) {
      super(pExecutedSuccessfully, pErrorMessage);
    }

    public DummyClientRequestReport(int pTotalOperations, int pCurrentOperation, int pTotalParts, int pCurrentPart, String pDescription) {
      super(pTotalOperations, pCurrentOperation, pTotalParts, pCurrentPart, pDescription);
    }
    
  }
  
  @Test
  public void testResult() {
    DummyClientRequestReport report = new DummyClientRequestReport();
    assertTrue(report.isResult());
    assertTrue(!report.isErrorReport());
    assertTrue(!report.isProgressReport());
  }
  
  @Test
  public void testProgressReport() {
    DummyClientRequestReport report = new DummyClientRequestReport(1, 2, 3, 4, "desc");
    assertTrue(!report.isResult());
    assertTrue(!report.isErrorReport());
    assertTrue(report.isProgressReport());    
  }
  
  @Test
  public void testErrorReport() {    
    DummyClientRequestReport report = new DummyClientRequestReport(false, null);
    assertTrue(!report.isResult());
    assertTrue(report.isErrorReport());
    assertTrue(!report.isProgressReport());
    assertTrue(!report.executedSuccessfully());
    assertTrue(report.getErrorMessage() == null);      
  }
}
