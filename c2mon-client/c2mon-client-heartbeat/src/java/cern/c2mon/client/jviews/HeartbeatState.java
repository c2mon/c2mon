package cern.c2mon.client.jviews;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.listener.HeartbeatListener;
import cern.c2mon.shared.client.supervision.Heartbeat;

public class HeartbeatState extends JPanel implements  HeartbeatListener  {
  /**
   * Serial Version UID for the HeartbeatState class
   */
  private static final long serialVersionUID = 1289484182356285473L;
  private static final int HEARTBEAT_UNINITIALISED = 0;
  private static final int HEARTBEAT_OK = 1;
  private static final int HEARTBEAT_LOST = 2;

  private static ImageIcon[] ICONS={null, null, null};
  private static final String[] ICON_NAMES = {"heartbeat_uninitialised.gif", "heartbeat_ok.gif", "heartbeat_lost.gif"};

  private DateFormat heartbeatTimeFormat = new SimpleDateFormat("HH:mm");
  private DateFormat heartbeatFullTimeFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

  private JLabel labelHeartbeatTime = new JLabel();
  private JLabel labelHeartbeatIcon = new JLabel();
  
  private String description = "Waiting for first heartbeat...";
  
  /**
   * Default constructor
   */
  public  HeartbeatState() {
    init();
  }
  
  /**
   * Initializes the heartbeat state toolbar
   */
  private void init() {
    labelHeartbeatIcon.setMaximumSize(new Dimension(30, 25));
    labelHeartbeatIcon.setMinimumSize(new Dimension(30, 25));
    labelHeartbeatIcon.setSize(new Dimension(30, 25));
    labelHeartbeatIcon.setHorizontalAlignment(JLabel.CENTER);

    labelHeartbeatTime.setMaximumSize(new Dimension(100, 25));
    labelHeartbeatTime.setMinimumSize(new Dimension(100, 25));
    labelHeartbeatTime.setHorizontalAlignment(JLabel.CENTER);

    this.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
    this.add(labelHeartbeatTime);
    this.add(labelHeartbeatIcon);
    
    ImageIcon icon = getIcon(HEARTBEAT_UNINITIALISED);
    String description = "Waiting for first heartbeat...";
    setNewHeartbeatInfo(null, icon, description);
    
    C2monServiceGateway.getSupervisionManager().addHeartbeatListener(this);
  }
  
  /**
   * @return The current heartbeat state description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Inner method to set the description and the icon of the Heartbeat state toolbar
   * @param pIcon The icon to be set
   * @param pDescription The description to be displayed
   */
  private synchronized void setNewHeartbeatInfo(final Heartbeat pHeartbeat, final ImageIcon pIcon, final String pDescription) {
    this.description = pDescription;
  
    labelHeartbeatTime.setToolTipText(pDescription);
    if (pHeartbeat != null) {
      labelHeartbeatTime.setText(heartbeatTimeFormat.format(pHeartbeat.getTimestamp()));
    }
    else {
      labelHeartbeatTime.setText("??:??");
    }
    
    labelHeartbeatIcon.setIcon(pIcon);
    labelHeartbeatIcon.setToolTipText(pDescription);
  }

  private static ImageIcon getIcon(int pState) {
    if (ICONS[pState] == null) {
      Image image = Toolkit.getDefaultToolkit().getImage(ConnectionState.class.getClassLoader().getResource(ICON_NAMES[pState]));
      image = image.getScaledInstance(20,20, Image.SCALE_AREA_AVERAGING);
      ICONS[pState] = new ImageIcon(image);
    }
    return ICONS[pState];
  }

  public void onHeartbeatResumed(final Heartbeat pHeartbeat) {
    ImageIcon icon = getIcon(HEARTBEAT_OK);
    String description = "<HTML><BODY><P>Server heartbeat <B>RESUMED</B></P><P>Last heartbeat received at "
      + heartbeatFullTimeFormat.format(pHeartbeat.getTimestamp()) + "</P></BODY></HTML>";
    setNewHeartbeatInfo(pHeartbeat, icon, description);
  }

  public void onHeartbeatReceived(final Heartbeat pHeartbeat) {
    ImageIcon icon = getIcon(HEARTBEAT_OK);
    String description = "<HTML><BODY><P>Server heartbeat <B>OK</B></P><P>Last heartbeat received at "
      + heartbeatFullTimeFormat.format(pHeartbeat.getTimestamp()) + "</P></BODY></HTML>";
    setNewHeartbeatInfo(pHeartbeat, icon, description);
    
  }

  public void onHeartbeatExpired(final Heartbeat pHeartbeat) {
    ImageIcon icon = getIcon(HEARTBEAT_LOST);
    String description = "<HTML><BODY><P>Server heartbeat <B>expired</B> at "
      + heartbeatFullTimeFormat.format(pHeartbeat.getTimestamp())
      + "</P><P>Please contact TIM support if the problem persists for more than 5 minutes.</P></BODY></HTML>";
    setNewHeartbeatInfo(pHeartbeat, icon, description);
  }

}