/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project.
 * See http://ts-project-tim.web.cern.ch
 *
 * Copyright (C) 2008  CERN
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.video;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.UnknownHostException;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import cern.c2mon.client.auth.LoginToolBar;
import cern.c2mon.client.common.video.VideoConnectionProperties;
import cern.c2mon.client.common.video.VideoConnectionPropertiesCollection;
import cern.c2mon.client.jviews.ConnectionStateToolbar;
import cern.c2mon.client.video.actions.AboutTimDialogAction;
import cern.c2mon.client.video.actions.CloseAllCameraConnectionsAction;
import cern.c2mon.client.video.actions.HelpWebPageAction;
import cern.c2mon.client.video.actions.OpenCameraConnectionAction;
import cern.c2mon.client.video.actions.RefreshAction;
import cern.c2mon.client.video.actions.RestartAction;
import cern.c2mon.client.video.actions.SystemExitAction;

/**
 * This class contains the Main method to launch the TIM Video viewer application.
 * External parameters can control to which data tags it should listen.
 * 
 * @author Matthias Braeger
 */
public final class TimVideoViewer extends JFrame {
  
  /** Log4j instance */
  private static final Logger LOG = Logger.getLogger(TimVideoViewer.class);
  
  /**
   * The serial Version UID of the class. Needed for Java 1.6
   */
  private static final long serialVersionUID = -7687938210179552069L;
  
  /** Main video panel */
  private DefaultMainPanel videoMainPanel;
  
  /** instance of the video main handler */
  private VideoMainHandler videoHandler = null;
  
  /** the title for the video panel */
  private String title = null;
  
  /** View menu that contains also the available cameras, once the user has logged in.*/
  private JMenu menuView = null;
  
  /** Determines whether to display the "keys taken" counter or not */
  private boolean keysTakenCounter = true;
  
  /** The amount of video viewers on the right side */
  private int videoQueueSize = 4;
  
  /** if set to false the application accepts requests from different hosts */
  private boolean singleHostController = false;

  /** The singleton instance of the TIM Video viewer */
  private static TimVideoViewer instance = null;
  
  /** Used to determine the Operating system */
  final boolean isWindowsOS = System.getProperty("os.name").toLowerCase().contains("windows");
  
  /**
   * @return This method returns the Singleton instance of the
   * TimVideoViewer
   */
  public static synchronized TimVideoViewer getInstance() {
    if (instance == null)
      instance = new TimVideoViewer();

    return instance;
  }
  /**
   * Default constructor
   */
  private TimVideoViewer() {
    super();
    // Set the icon for the Task Bar
    Image icon = Toolkit.getDefaultToolkit().getImage(TimVideoViewer.class.getResource("TIM_icon.gif"));
    this.setIconImage(icon);
    
    if (isWindowsOS) {
      // Set the system look and feel for Windows
      try {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      } catch (Exception e) {
        LOG.error("Error setting native LAF: " + e);
      }
    }

    loadUserConfiguration();
    
    if (title == null) {
      title = "TIM Video Viewer";
      setTitle(title);
    }
    else { 
      // Set the title of the window
      setTitle("TIM Video Viewer (" + title + ")");
    }

    // Try to retrieve height and width from the configuration file 
    Integer height = null;
    Integer width = null;
    try {
      height = (Integer) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.SCREEN_HEIGHT);
    } catch (Exception e) {
      LOG.error("Wrong value for " + VideoPropertyNames.SCREEN_HEIGHT + " defined in configuration file.");
    }
    try {
      width = (Integer) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.SCREEN_WIDTH);
    } catch (Exception e) {
      LOG.error("Wrong value for " + VideoPropertyNames.SCREEN_WIDTH + " defined in configuration file.");
    }
    
    // Calculate window size
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    if (height != null) {
      dim.height = height;
    }
    else {
      if (isWindowsOS)
        dim.height = dim.height - 30; // We have to let a bit of space for the windows task bar
      else 
        dim.height = dim.height - 50; // We have to let a bit of space for the windows task bar
    }
    
    if (width != null) {
      dim.width = width;
    }
    
    setSize(dim);
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    getContentPane().setLayout(new BorderLayout());
  }
  
  
  /**
   * Initializes the main panel which contains the video viewers
   */
  private void initialize() {
    final Boolean createVlcViewer = 
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VLC_PLAYER);
    // Create the video panel and add it to the view
    if (createVlcViewer) {
      videoMainPanel = 
        new DefaultMainPanel(title, keysTakenCounter, videoQueueSize);
    }
    else {
      videoMainPanel = 
        new NetvuMainPanel(title, keysTakenCounter, videoQueueSize);
    }
    getContentPane().add(videoMainPanel, BorderLayout.CENTER);

    createMenu();
    createToolbarItems();

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(final WindowEvent e) {
        videoHandler.closeAllConnections();
        System.exit(0);
      }
    });

    setVisible(true);

    // Close splash screen and set pointer to null
    TVVSplashScreen.setVisible(false);
    
    // only now we create the the controllers
    createApplicationControllers();
  }

  /**
   * Shows a dialog to the user to enter a valid host name. The application
   * will then only accept request coming from that host.
   * @return The name of the controller host
   */
  private String showVideoRequestorHostDialog() {
    String controllerHostName = "";
    String message; 
    boolean wrongHostName = true;

    while (wrongHostName) {
      message = 
        "Please enter the name of the host that is running\n"
        + "the control panel of the TIM Video Viewer:";

      controllerHostName = JOptionPane.showInputDialog(this, message);
      if (controllerHostName == null) {
        this.setVisible(false);
        System.exit(0);
      }
      else if (controllerHostName.equalsIgnoreCase("")) {
        message = "You did not specify a host name! "
                  + "The TIM Video Viewer will not work without a valid one."
                  + "\n\nPress Yes, if you want to continue and add a host name?\n"
                  + "Press No, if you prefer to to exit:";
        int answer = 
          JOptionPane.showConfirmDialog(this, message, "Message", JOptionPane.YES_NO_OPTION);
        
        if (answer == JOptionPane.NO_OPTION) {
          this.setVisible(false);
          System.exit(0);  
        }
      }
      else
        wrongHostName = false;
    }
    
    return controllerHostName;
  }
  
  /**
   * Creates the menu items of the view menu. 
   * @param connections Creates for each item of this collection a menu entry. In case
   *                    that the parameter is <code>null</code> the method creates only
   *                    the standard entries.
   */
  public void createViewMenuItems(final VideoConnectionPropertiesCollection connections) {
    menuView.removeAll();
    
    if (isWindowsOS) {
      final JMenuItem refreshItem = new JMenuItem(RefreshAction.getInstance());
      refreshItem.setAccelerator(KeyStroke.getKeyStroke("F5"));
      menuView.add(refreshItem);
      
      final JMenuItem restartItem = new JMenuItem(RestartAction.getInstance());
      restartItem.setAccelerator(KeyStroke.getKeyStroke("F12"));
      menuView.add(restartItem);
      
      menuView.add(new JSeparator());
    }
    menuView.add(CloseAllCameraConnectionsAction.getInstance());
    
//  menuView.add(new JSeparator());
//  final JMenuItem testItem1 = new JMenuItem(TestVlcViewerAction.getInstance());
//  menuView.add(testItem1);
//  
//  final JMenuItem testItem3 = new JMenuItem(SwitchMainViewAction.getInstance());
//  menuView.add(testItem3);
//  
//  final JMenuItem testItem4 = new JMenuItem(TestCloseViewAction.getInstance());
//  menuView.add(testItem4);
//  
//  menuView.add(new JSeparator());
//  
//  final JMenuItem testItem2 = new JMenuItem(CloseVideosAction.getInstance());
//  menuView.add(testItem2);
    
    if (connections != null) {
      menuView.add(new JSeparator());
      Iterator< ? > iter = connections.iterator();
      VideoConnectionProperties vcp = null;
      while (iter.hasNext()) {
        vcp = (VideoConnectionProperties) iter.next();
        menuView.add(new OpenCameraConnectionAction(vcp));
      }
    }
  }
  
  /**
   * Creates the menu bar and all sub items
   */
  private void createMenu() {
    final JMenuBar menuBar = new JMenuBar();
    menuBar.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createEtchedBorder(),
        BorderFactory.createEmptyBorder(0, 0, 0, 10)));
    
    final JMenu menuFile = new JMenu("File");
    menuFile.setMnemonic(KeyEvent.VK_F);
    menuBar.add(menuFile);
    
    final JMenuItem exitItem = new JMenuItem(SystemExitAction.getInstance());
    menuFile.add(exitItem);
    
    menuView = new JMenu("View");
    menuView.setMnemonic(KeyEvent.VK_V);
    menuView.addMenuListener(new MenuListener() {
      @Override
      public void menuSelected(final MenuEvent e) {
        // This is needed to solve a bug from java
        menuView.invalidate();
        menuView.revalidate();
      }
      
      @Override
      public void menuDeselected(final MenuEvent e) {
        // Do nothing
      }
      
      @Override
      public void menuCanceled(final MenuEvent e) {
        // Do nothing
      }
    });
    menuBar.add(menuView);
    createViewMenuItems(null);
    
    final JMenu menuHelp = new JMenu("Help");
    menuHelp.setMnemonic(KeyEvent.VK_H);
    menuBar.add(menuHelp);
    
    final JMenuItem helpItem = new JMenuItem(HelpWebPageAction.getInstance());
    helpItem.setAccelerator(KeyStroke.getKeyStroke("F1"));
    
    menuHelp.add(helpItem);
    menuHelp.addSeparator();
    final AbstractAction aboutTimDialogAction = new AboutTimDialogAction(this);
    menuHelp.add(new JMenuItem(aboutTimDialogAction)); 

    setJMenuBar(menuBar);
  }

  /**
   * Adds the tool bar items to the 
   */
  private void createToolbarItems() {
    final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    final LoginToolBar loginBar = new LoginToolBar(this);
    loginBar.setFocusable(false);
    final ConnectionStateToolbar heartbeatBar = new ConnectionStateToolbar(this);
    heartbeatBar.setFocusable(false);
    
    panel.add(loginBar);
    panel.add(heartbeatBar);
    getContentPane().add(panel, BorderLayout.NORTH);
  }


  /**
   * Creates an instance of the VideoConnectionRequester and
   * the VideoMainHandler
   */
  private void createApplicationControllers() {
    // Create the handler
    videoHandler = new VideoMainHandler(videoMainPanel);
   
    
    if (singleHostController) {
      boolean wrongHostName = true;
      String hostName;
      while (wrongHostName) {
        hostName = showVideoRequestorHostDialog();
        
        try {
          new VideoConnectionRequester(videoHandler, hostName);
          JOptionPane.showMessageDialog(this, 
              "Control panel host " + hostName + " successfully registered!");
          wrongHostName = false;
        } catch (UnknownHostException e) {
          String message = "You entered an invalid host name! "
            + "The TIM Video Viewer will not work without a valid one."
            + "\n\nPress Yes, if you want to continue and add a host name?\n"
            + "Press No, if you prefer to to exit:";
          int answer = 
            JOptionPane.showConfirmDialog(this, message, "Error", JOptionPane.YES_NO_OPTION);
          
          if (answer == JOptionPane.NO_OPTION) {
            this.setVisible(false);
            System.exit(0);  
          }
        }
      }
    }
    else { // If we are here, we accept requests from every host
      try { 
        new VideoConnectionRequester(videoHandler);
      } catch (UnknownHostException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Loads the user configuration.
   */
  private void loadUserConfiguration() {
    // Retrieve title from user configuration
    title = 
      (String) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VIDEO_VIEWER_TITLE);
    
    // Load the user configuration
    keysTakenCounter = 
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.KEYS_TAKEN_COUNTER);
    
    videoQueueSize = 
      (Integer) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.VIDEO_QUEUE_SIZE);
    
    singleHostController =
      (Boolean) VideoViewerConfigLoader.getInstance().getPropertyByName(
          VideoPropertyNames.SINGLE_HOST_CONTROLLER);
  }
  
  /**
   * @return the video main panel controller
   */
  public VideoPanelController getVideoPanelController() {
    return videoMainPanel;
  }
  
  /**
   * @return the video main panel controller
   */
  public VideoMainHandler getVideoMainHandler() {
    return videoHandler;
  }
  
  /**
   * Use this main method to start the TIM Video Viewer and starts the
   * splash screen
   * @param args
   */
  public static void main(String[] args) {
    // Set up a simple configuration that logs on the console.
    BasicConfigurator.configure();
    
    TVVSplashScreen.setVisible(true);
    TimVideoViewer viewer = TimVideoViewer.getInstance();
    try {
      viewer.initialize();
    } catch (UnsatisfiedLinkError e) {
      TVVSplashScreen.setVisible(false);
      String message = 
        "An error occured while trying to start the TIM Video Viewer. "
        + "Please try first to fix it:\n" + e.getMessage();
      JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }
}
