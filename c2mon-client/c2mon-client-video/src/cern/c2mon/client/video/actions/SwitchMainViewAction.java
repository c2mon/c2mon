package cern.c2mon.client.video.actions;

import java.awt.event.ActionEvent;
import java.util.Random;

import javax.swing.AbstractAction;

import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.video.TimVideoViewer;

public class SwitchMainViewAction extends AbstractAction {
  
  /** Singelton instance */
  private static SwitchMainViewAction instance = null;
  
  private final VideoConnectionProperties[] openViews = new VideoConnectionProperties[1];
  
  /** Put here the right odserver */
  private final static String odserver = "";

  /**
   * Hidden default constructor
   */
  protected SwitchMainViewAction() {
    super("Random Main View");
    
    /** Put here the right odserver */
    openViews[0] = new VideoConnectionProperties(odserver, 1234);
    openViews[0].setDescription(odserver +":1234");

  }
  
  public static AbstractAction getInstance() {
    if (instance == null) {
      instance = new SwitchMainViewAction();
    }
    
    return instance;
  }
  
  @Override
  public void actionPerformed(ActionEvent arg0) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        int count = 0;
        
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {}
        
        count = 0;
        VideoConnectionProperties conn = null;
        while (true) {
//          if (conn != null)
//            TimVideoViewer.getInstance().getVideoMainHandler().closeVideoConnection(conn);
          try {Thread.sleep(1000);} catch (InterruptedException e) {}
          final Random random = new Random();
          conn = openViews[random.nextInt(openViews.length)];
          TimVideoViewer.getInstance().getVideoMainHandler().setActiveVideoConncetion(conn);
          try {Thread.sleep(10000);} catch (InterruptedException e) {}
        }
      }
    }).start();   
  }
  
}
