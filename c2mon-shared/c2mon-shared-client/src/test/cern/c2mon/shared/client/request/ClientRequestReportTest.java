package cern.c2mon.shared.client.request;

import static org.junit.Assert.*;

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
  
  @Test(expected=UnsupportedOperationException.class)
  public void testExceptionOnMethodCallForResult() {
    DummyClientRequestReport report = new DummyClientRequestReport();
    report.getCurrentOperation();
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testExceptionOnMethodCallForProgress() {
    DummyClientRequestReport report = new DummyClientRequestReport(1, 2, 3, 4, "desc");
    report.executedSuccessfully();
  }
  
  @Test(expected=UnsupportedOperationException.class)
  public void testExceptionOnMethodCallForError() {
    DummyClientRequestReport report = new DummyClientRequestReport(true, "with description");
    report.getProgressDescription();
  }
  
  
}
