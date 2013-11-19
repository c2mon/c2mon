package cern.c2mon.shared.util.threadhandler;

import static org.easymock.EasyMock.*;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.shared.util.threadhandler.ThreadHandler;

public class ThreadHandlerTest {

  /**
   * Class to test.
   */
  private ThreadHandler threadHandler;

  /**
   * Object processing the incoming objects.
   */
  private MockProcessor mockProcessor;
  
  /**
   * Realistic processor for TIM.
   */
  private RealisticProcessor realisticProcessor;
  
  /**
   * Parameters passed to the handler.
   */
  private Object[] parameterArray;
  
  @Before
  public void setUp() throws SecurityException, NoSuchMethodException {
   mockProcessor = createMock(MockProcessor.class);
   Method method = MockProcessor.class.getMethod("processUpdate", new Class<?>[] {String.class, Long.class, int.class, Object.class});
   threadHandler = new ThreadHandler(mockProcessor, method);
   threadHandler.start();
   parameterArray = new Object[] {"test", Long.valueOf(0), 1, Short.valueOf("23")};
  }
    
  //@Test
  public void testPutting() {    
    mockProcessor.processUpdate("test", Long.valueOf(0), 1, Short.valueOf("23"));
    replay(mockProcessor);
    threadHandler.put(parameterArray);
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    verify(mockProcessor);
    threadHandler.shutdown();
  }
  
  /**
   * Does not crash.
   */
  @Test 
  public void feedIncorrectParameters() {
    //incorrect array for the method
    parameterArray = new Object[] {"test", Long.valueOf(0), 1};
    threadHandler.put(parameterArray);
    threadHandler.shutdown();
  }
  
  /**
   * Checks that a trivial handler can process one million method calls per second
   * when the passed object is a simple empty object.
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  //@Test
  public void performanceTest() throws SecurityException, NoSuchMethodException {
    //set up realistic processor    
    realisticProcessor = createMock(RealisticProcessor.class);
    Method method = RealisticProcessor.class.getMethod("processUpdate", new Class<?>[] {Object.class});
    threadHandler = new ThreadHandler(realisticProcessor, method);
    threadHandler.start();
    
    Object parameterObject = new Object();
    parameterArray = new Object[] {parameterObject};
    
    realisticProcessor.processUpdate(parameterObject);
    expectLastCall().times(1000000);
    replay(realisticProcessor);
    for (int i = 0; i < 1000000; i++) {
      threadHandler.put(parameterArray);
    }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    verify(realisticProcessor);
    threadHandler.shutdown();
  }
  
  /**
   * Same as performanceTest but with several parameters passed to the object.
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  //@Test
  public void performanceTestSeveralParameters() throws SecurityException, NoSuchMethodException {    
    
    mockProcessor.processUpdate("test", Long.valueOf(0), 1, Short.valueOf("23"));    
    expectLastCall().times(1000000);
    replay(mockProcessor);
    for (int i = 0; i < 1000000; i++) {
      threadHandler.put(parameterArray);
    }
    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    verify(mockProcessor);
    threadHandler.shutdown();
  }
  
  /**
   * Throws exception.
   */
  @Test(expected=IllegalStateException.class)
  public void testPuttingInDisabled() {    
    threadHandler.shutdown();    
    threadHandler.put(parameterArray);     
  }
  
  
}
